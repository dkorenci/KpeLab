package hr.irb.zel.kpelab.phrase;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Extract as phrases tokens sequences with Adj*N+ POS patterns. 
 */
public class PosRegexPhraseExtractor implements IPhraseExtractor {

    /** Init with canonic form and default processing components. */
    public PosRegexPhraseExtractor(CanonicForm c) throws ResourceInitializationException {
        config = new PosExtractorConfig(Components.STANFORD, c);        
    }
    
    /** Init with full configuration. */
    public PosRegexPhraseExtractor(PosExtractorConfig conf) {        
        config = conf;
    }
    
    public String getId() { return "posRegexPhext"; }
 
    private void createEngines() throws UIMAException {
//        segmenter = createEngine(createEngineDescription(StanfordSegmenter.class));    
//        lemmatizer = createEngine(createEngineDescription(StanfordLemmatizer.class));        
//        posTagger = createEngine(createEngineDescription(StanfordPosTagger.class));  
//        stemmer = createEngine(createEngineDescription(SnowballStemmer.class));  
//        stopwords = createEngine(
//                createEngineDescription(StopWordRemover.class, 
//                StopWordRemover.PARAM_STOP_WORD_LIST_FILE_NAMES, "resources/snowball.stopwords.en.txt")
//                );        
        //posTagger = createEngine(createEngineDescription(OpenNlpPosTagger.class));        
    }    
    
    /** Extract phrases, return list of non-duplicate phrases. */
    public List<Phrase> extractPhrases(String text) throws UIMAException  {
        this.text = text;        
        preprocess();        
        extract();
        //removeSubphrases();        
        return phrases;
    }
    
    private String text; // text from which to extract phrases
    private JCas jCas; // results from DKPro pipeline    
    
    private PosExtractorConfig config;    
    
    private List<Phrase> phrases;       
    
    private static final int MAX_PHRASE_LENGTH = 4;

    private void preprocess() throws UIMAException {
        jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
 //output AE for testing        
//        AnalysisEngine fileDump = createEngine(createEngineDescription(
//            CasDumpWriter.class,
//            CasDumpWriter.PARAM_OUTPUT_FILE, "output.txt"));
        if (config.canonic == CanonicForm.LEMMA) 
            runPipeline(jCas, config.segmenter, config.posTagger, config.lemmatizer);                        
        else if (config.canonic == CanonicForm.STEM)
            runPipeline(jCas, config.segmenter, config.posTagger, config.stemmer);     
        else if (config.canonic == CanonicForm.NO_CANNONIZATION)
            runPipeline(jCas, config.segmenter, config.posTagger);
        else throw new IllegalArgumentException("canonic form not covered");
    }

    // extract noun phrases - token sequeces that have the form Adj*Noun+
    private void extract() {
        initPhrases();
        int tokenCnt = 0; // token index at document level
        // iterate over all sentences in document jCas
        for (Sentence sentence : select(jCas, Sentence.class)) {
            // get all tokens in the sentence
            List<Token> tokens = selectCovered(jCas, Token.class, sentence);            
            boolean adjs = false, nouns = false; // unbroken sequence of Adjs or Nouns
            int npSentStart = 0; // start of the noun phrase relative to sentence
            int npDocStart = 0; // start of the noun phrase relative to document
            for (int i = 0; i < tokens.size(); ++i) {
                Token tok = tokens.get(i);
                if (!isCorruptToken(tok)) tokenCnt++;                 
                boolean isNoun = isNoun(tok), isAdj = isAdj(tok);
                // stop building a phrase is token is corrupt
                if (isCorruptToken(tok)) { isNoun = false; isAdj = false; }
                
                if (isNoun) {                    
                    if (!adjs && !nouns) { // start new phrase
                        npSentStart = i;
                        npDocStart = tokenCnt;
                    } 
                    nouns = true; adjs = false;
                }
                else if (isAdj) {
                    if (nouns) { 
                        processSubphrases(tokens, npSentStart, i-1, npDocStart); // end of phrase
                        // start new phrase (adjective part)
                        nouns = false; adjs = true; 
                        npSentStart = i; npDocStart = tokenCnt;
                    }
                    else if (!adjs) { // start new phrase
                        adjs = true; npSentStart = i; npDocStart = tokenCnt; 
                    }
                }
                else {
                    if (nouns) processSubphrases(tokens, npSentStart, i-1, npDocStart);
                    nouns = false; adjs = false;
                }                                
            }            
        }
    }

    private boolean isCorruptToken(Token tok) {
        String txt = tok.getCoveredText();
        return !Utils.isWord(txt) || txt.length() <= 1;
    }    
    
    // process all valid subphrases Adj*N+ sequences shorter than MAX_PHRASE_LENGTH
    private void processSubphrases(List<Token> sentence, int start, int end, int docStart) {
        // calc index of first noun
        int firstNoun = -1;
        for (int i = start; i <= end; ++i) {
            Token t = sentence.get(i);
            if (isNoun(t)) { firstNoun = i; break; }
        }
        assert(firstNoun != -1);
        // find valid subphrases (as [i,j] token ranges)
        for (int i = start; i <= end; ++i) {
            for (int j = i; j <= end; ++j) {
                if (j >= firstNoun && j-i+1 <= MAX_PHRASE_LENGTH) {
                    processPhrase(sentence, i, j, docStart);
                }
            }
        }
    }
    
    // form phrase from sentence token coordinates, add to phrase register
    private void processPhrase(List<Token> sentence, int start, int end, int docStart) {
        Phrase phrase = new Phrase();
        phrase.setFirstOccurence(docStart);
        phrase.setFrequency(1);
        List<String> tokens = new ArrayList<String>();
        List<String> ctokens = new ArrayList<String>();
        // if phrase is too long, cut (tokens at start) to max. length
        if (end - start + 1 > MAX_PHRASE_LENGTH) { 
            start = end - MAX_PHRASE_LENGTH + 1;
        }
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
        
        addPhrase(phrase);
    }    
    
    private void initPhrases() {
        phrases = new ArrayList<Phrase>();
    }    
    
    // update set of phrases with a new phrase
    private void addPhrase(Phrase newPhrase) {
        for (Phrase ph : phrases) {
            if (ph.equals(newPhrase)) {
                ph.setFrequency(ph.getFrequency() + 1);
                //System.out.println(ph + " new freq: " + ph.getFrequency());
                return;
            }
        }
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

    private void IllegalArgumentException(String uncovered_canonic_form) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
