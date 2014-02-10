/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import java.util.ArrayList;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import org.apache.uima.fit.factory.JCasFactory;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 *
 */
public class PhraseHelper {    
    
    static boolean enginesInitialized;
    static AnalysisEngine segmenter, lemmatizer, stemmer;        
    
    /** Tokenize wordList, lemmatize and return tokens */
    public static List<TokenCanonic> getCanonicForms(String wordList, CanonicForm canonicForm) 
            throws UIMAException {                
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText(wordList);
        jCas.setDocumentLanguage("en");
        initEngines();

        if (canonicForm == CanonicForm.LEMMA) 
            runPipeline(jCas, segmenter, lemmatizer);            
        else
            runPipeline(jCas, segmenter, stemmer);            
        
        List<TokenCanonic> result = new ArrayList<TokenCanonic>();        
        for (Token tok : select(jCas, Token.class)) {
            TokenCanonic tl = new TokenCanonic();
            if (canonicForm == CanonicForm.LEMMA) tl.canonic = tok.getLemma().getValue();
            else tl.canonic = tok.getStem().getValue();
            tl.token = tok.getCoveredText();
            result.add(tl);
        }
                
        return result;
    }
    
    public static String stemWord(String word) {
        if (word.matches(".*\\s.*")) throw 
                new IllegalArgumentException("argument is not a single word: " + word);        
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
        // slow way of doing it with jcas
//        List<TokenCanonic> c = getCanonicForms(word, CanonicForm.STEM);
//        return c.get(0).canonic;
    }

    /** Return number of words in a document. */
    public static int countWords(String text) throws UIMAException {                
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
        initEngines();

        runPipeline(jCas, segmenter);            
        
        int cnt = 0;       
        for (Token tok : select(jCas, Token.class)) {
            String token = tok.getCoveredText();
            if (token.matches("\\p{Alpha}*")) cnt++;
        }
                
        return cnt;
    }    
    
    private static void initEngines() throws ResourceInitializationException {
        if (!enginesInitialized) {
            segmenter = createEngine(createEngineDescription(StanfordSegmenter.class));
            lemmatizer = createEngine(createEngineDescription(StanfordLemmatizer.class));   
            stemmer = createEngine(createEngineDescription(SnowballStemmer.class));
        }
    }

    public static void printPhraseSet(List<Phrase> phrases, int phrasesPerRow) {
        int ppr = 0;
        for (int i = 0; i < phrases.size(); ++i) {
            Phrase ph = phrases.get(i);
            System.out.print(ph + " ; ");
            if (++ppr == phrasesPerRow) {
                ppr = 0;
                if (i == phrases.size() - 1) {
                    System.out.println();
                } else {
                    System.out.println();
                }
            }
        }
        if (ppr > 0) {
            System.out.println();
        }
    }
    
    // token and its canonic form
    public static class TokenCanonic {
        public String token;
        public String canonic;
    }
    
}
