package hr.irb.zel.kpelab.phrase;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import java.util.ArrayList;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.CasDumpWriter;
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
    
    public List<Phrase> extractPhrases(String text) throws UIMAException  {
        this.text = text;        
        preprocess();        
        extract();
        return phrases;
    }
    
    private String text; // text from which to extract phrases
    private JCas jCas; // results from DKPro pipeline    
    
    private PosExtractorConfig config;    
    
    private List<Phrase> phrases;       

    private void preprocess() throws UIMAException {
        jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
 //output AE for testing        
//        AnalysisEngine fileDump = createEngine(createEngineDescription(
//            CasDumpWriter.class,
//            CasDumpWriter.PARAM_OUTPUT_FILE, "output.txt"));
        if (config.canonic == CanonicForm.LEMMA) 
            runPipeline(jCas, config.segmenter, config.lemmatizer, config.posTagger);                        
        else if (config.canonic == CanonicForm.STEM)
            runPipeline(jCas, config.segmenter, config.stemmer, config.posTagger);                        
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
                tokenCnt++;
                Token tok = tokens.get(i);
                boolean isNoun = isNoun(tok), isAdj = isAdj(tok);
                if (isNoun) {                    
                    if (!adjs && !nouns) { // start new phrase
                        npSentStart = i;
                        npDocStart = tokenCnt;
                    } 
                    nouns = true; adjs = false;
                }
                else if (isAdj) {
                    if (nouns) { 
                        processPhrase(tokens, npSentStart, i-1, npDocStart); // end of phrase
                        // start new phrase (adjective part)
                        nouns = false; adjs = true; 
                        npSentStart = i; npDocStart = tokenCnt;
                    }
                    else if (!adjs) { // start new phrase
                        adjs = true; npSentStart = i; npDocStart = tokenCnt; 
                    }
                }
                else {
                    if (nouns) processPhrase(tokens, npSentStart, i-1, npDocStart);
                    nouns = false; adjs = false;
                }                                
            }            
        }
    }

    private void processPhrase(List<Token> sentence, int start, int end, int docStart) {
        Phrase phrase = new Phrase();
        phrase.setFirstOccurence(docStart);
        phrase.setFrequency(1);
        List<String> tokens = new ArrayList<String>();
        List<String> ctokens = new ArrayList<String>();
        for (int i = start; i <= end; ++i) {
            Token t = sentence.get(i);            
            tokens.add(t.getCoveredText());
            if (config.canonic == CanonicForm.LEMMA) ctokens.add(t.getLemma().getValue());            
            else ctokens.add(t.getStem().getValue());
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
                return;
            }
        }
        phrases.add(newPhrase);
    }
    
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
   
}
