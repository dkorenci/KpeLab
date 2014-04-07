/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.util.Utils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Extract phrases as sequences of words (ngrams) delimited
 * by end of sentence, non-word tokens and stop words.
 */
public class NgramPhraseExtractor implements IPhraseExtractor {
      
    public NgramPhraseExtractor(PosExtractorConfig conf) {        
        config = conf;
    }
    
    public String getId() { return "ngramPhext"; }
    
    /** Extract phrases, return list of non-duplicate phrases. */
    public List<Phrase> extractPhrases(String text) throws UIMAException, IOException  {
        this.text = text;     
        readWordLists();
        preprocess();        
        extract();        
        calcRelativeOccurences();
        return phrases;
    }
    
    private String text; // text from which to extract phrases
    private JCas jCas; // results from DKPro pipeline    
    
    private PosExtractorConfig config;    
    
    private List<Phrase> phrases;       
    private Set<String> stopwords;
    private Set<String> insidewords;    
    
    private static final int MAX_PHRASE_LENGTH = 8;
    private int tokenCnt;

    private void preprocess() throws UIMAException {
        jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
 //output AE for testing        
//        AnalysisEngine fileDump = createEngine(createEngineDescription(
//            CasDumpWriter.class,
//            CasDumpWriter.PARAM_OUTPUT_FILE, "output.txt"));
        if (config.canonic == CanonicForm.LEMMA) 
            runPipeline(jCas, config.segmenter, config.lemmatizer);                        
        else if (config.canonic == CanonicForm.STEM)
            runPipeline(jCas, config.segmenter, config.stemmer);     
        else if (config.canonic == CanonicForm.NO_CANNONIZATION)
            runPipeline(jCas, config.segmenter);
        else throw new IllegalArgumentException("canonic form not covered");
    }

    private void readWordLists() throws IOException {
        if (stopwords == null) {
            stopwords = new TreeSet<String>();        
            readWordList(stopwords, "phrase.stopwords");
        }
        if (insidewords == null) {
            insidewords = new TreeSet<String>();
            readWordList(insidewords, "phrase.insidewords");
        }
    }
    
    // read list of words from a text file, one word per line, store in a set
    private void readWordList(Set<String> words, String setId) throws IOException {
        String stopwordFile = KpeConfig.getProperty(setId);
        BufferedReader reader = new BufferedReader(new FileReader(stopwordFile));
        String line;
        while ((line = reader.readLine()) != null) {
            words.add(line.trim());
        }
    }
    
    // extract noun phrases - token sequeces that have the form Adj*Noun+
    private void extract() {
        initPhrases();
        tokenCnt = 0; // token index at document level
        // iterate over all sentences in document jCas
        for (Sentence sentence : select(jCas, Sentence.class)) {
            // get all tokens in the sentence
            List<Token> tokens = selectCovered(jCas, Token.class, sentence);                        
            int npSentStart = 0; // start of the noun phrase relative to sentence
            int npDocStart = 0; // start of the noun phrase relative to document
            boolean phraseStarted = false;
            for (int i = 0; i < tokens.size(); ++i) {
                Token tok = tokens.get(i);
                if (!isCorruptToken(tok)) tokenCnt++;                 
                
                if (phraseStarted) {
                    if (!isPhraseToken(tok)) {
                        processSubphrases(tokens, npSentStart, i-1, npDocStart);
                        phraseStarted = false;
                    }
                    else continue;
                }
                else {
                    if (isPhraseToken(tok)) {
                        npSentStart = i; npDocStart = tokenCnt;
                        phraseStarted = true;                        
                    }
                    else continue;
                }                                
            }
            if (phraseStarted) processSubphrases(tokens, npSentStart, tokens.size()-1, npDocStart);
        }
    }

    private boolean isPhraseToken(Token tok) {
        return !isCorruptToken(tok) && !isStopWord(tok);
    }
    
    // word that cannot be contained in a phrase
    private boolean isStopWord(Token tok) {
        String txt = tok.getCoveredText().trim().toLowerCase();        
        return stopwords.contains(txt) || txt.length() <= 1;
    }
    
    private boolean isCorruptToken(Token tok) {
        String txt = tok.getCoveredText();
        return !Utils.isWord(txt) || txt.length() == 0;
    }    
    
    // word that cannot start or end the phrase
    private boolean isInsideWord(Token tok) {
        String txt = tok.getCoveredText().trim().toLowerCase();        
        return insidewords.contains(txt) || txt.length() <= 2;        
    }
    
    // process all valid subphrases Adj*N+ sequences shorter than MAX_PHRASE_LENGTH
    private void processSubphrases(List<Token> sentence, int start, int end, int docStart) {
        // find valid subphrases (as [i,j] token ranges)
        for (int i = start; i <= end; ++i) {
            for (int j = i; j <= end; ++j) {
                if (j-i+1 <= MAX_PHRASE_LENGTH) {
                    boolean firstLevel;
                    if (i == start && j == end) firstLevel = true;
                    else firstLevel = false;
                    processPhrase(sentence, i, j, docStart, firstLevel);
                }
            }
        }
    }
    
    // form phrase from sentence token coordinates, add to phrase register
    private void processPhrase(List<Token> sentence, int start, int end, 
            int docStart, boolean firstLevel) {
        if (isInsideWord(sentence.get(start)) || isInsideWord(sentence.get(end))) return;
        Phrase phrase = new Phrase();
        phrase.setFirstOccurence(docStart);
        phrase.setFrequency(1);
        List<String> tokens = new ArrayList<String>();
        List<String> ctokens = new ArrayList<String>();
        for (int i = start; i <= end; ++i) {
            Token t = sentence.get(i);            
            tokens.add(t.getCoveredText());
            String tokStr;
            if (config.canonic == CanonicForm.LEMMA) tokStr = t.getLemma().getValue();                                      
            else if (config.canonic == CanonicForm.STEM) tokStr = t.getStem().getValue();            
            else if (config.canonic == CanonicForm.NO_CANNONIZATION) tokStr = t.getCoveredText();
            else throw new IllegalArgumentException("uncovered canonic form");
            // fallback to original text if canonization was unsuccessful
            if (tokStr == null) tokStr = t.getCoveredText();
            tokStr = tokStr.toLowerCase();
            ctokens.add(tokStr);
        }
        phrase.setTokens(tokens);
        phrase.setCanonicTokens(ctokens);
        
        addPhrase(phrase, firstLevel);
    }    
    
    private void initPhrases() {
        phrases = new ArrayList<Phrase>();
    }    
    
    // update set of phrases with a new phrase
    private void addPhrase(Phrase newPhrase, boolean firstLevel) {
        for (Phrase ph : phrases) {
            if (ph.equals(newPhrase)) {
                ph.setFrequency(ph.getFrequency() + 1);
                if (firstLevel) ph.setFirstLevel(true);
                //System.out.println(ph + " new freq: " + ph.getFrequency());
                return;
            }
        }
        newPhrase.setFirstLevel(firstLevel);
        phrases.add(newPhrase);
        //System.out.println(newPhrase);
    }
    
    // remove subphrases that occur only as part of a superphrase
//    private void removeSubphrases() {        
//        Iterator<Phrase> it = phrases.iterator();
//        while (it.hasNext()) {
//            Phrase ph = it.next();
//            boolean remove = false;
//            // remove phrase if there is a superphrase with equal frequency
//            for (Phrase phr : phrases) {
//                if (ph.isSubphrase(phr)) {
//                    //assert(ph.getFrequency() >= phr.getFrequency());                    
//                    if (ph.getFrequency() == phr.getFrequency()) {
//                        remove = true;
//                        break;
//                    }                    
//                }
//            }
//            if (remove) it.remove();
//        }
//    }
    
    private void print() {
        for (Sentence sentence : select(jCas, Sentence.class)) {
            List<Token> tokens = selectCovered(jCas, Token.class, sentence);
            System.out.println("-------------------");
            System.out.println(sentence.getCoveredText());
            for (Token tok : tokens) {
                System.out.print(tok.getCoveredText() + ":" + tok + "|");
            }
            System.out.println();
        }
    }    

    private boolean isNoun(Token tok) {
        return (tok.getPos() instanceof N);
    }

    private boolean isAdj(Token tok) {
        return (tok.getPos() instanceof ADJ);
    }

    private void calcRelativeOccurences() {
        for (Phrase ph : phrases) {
            ph.setRelFirstOccurence(ph.getFirstOccurence()/((double)tokenCnt));
        }
    }    
    
    private void IllegalArgumentException(String uncovered_canonic_form) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
