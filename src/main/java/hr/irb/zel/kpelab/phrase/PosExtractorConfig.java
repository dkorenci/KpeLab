/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Processing components (UIMA analysis engines) and processing options.
 */
public class PosExtractorConfig {
    
    public CanonicForm canonic;
    public AnalysisEngine segmenter, lemmatizer, posTagger, stemmer;        
    
    /** Designates component suite. */
    public static enum Components { STANFORD, OPEN_NLP, CLEAR_NLP }    
    
    /** Construct with default parameters. */
    public PosExtractorConfig() throws ResourceInitializationException {
        canonic = CanonicForm.STEM; // default canonic form
        setStanfordNlpComponents();
    }
    
    public PosExtractorConfig(Components comp, CanonicForm canon) 
            throws ResourceInitializationException {
        canonic = canon;
        if (comp == Components.STANFORD) setStanfordNlpComponents();
        else if (comp == Components.OPEN_NLP) setOpenNlpComponents();
        else if (comp == Components.CLEAR_NLP) setClearNlpComponents();
        else throw new IllegalArgumentException("POS component suite not covered.");
    }
    
    private void setStanfordNlpComponents() 
            throws ResourceInitializationException {
        segmenter = createEngine(createEngineDescription(StanfordSegmenter.class));    
        lemmatizer = createEngine(createEngineDescription(StanfordLemmatizer.class));        
        posTagger = createEngine(createEngineDescription(StanfordPosTagger.class));  
        stemmer = createEngine(createEngineDescription(SnowballStemmer.class));              
    }

    private void setOpenNlpComponents() 
            throws ResourceInitializationException {        
        segmenter = createEngine(createEngineDescription(OpenNlpSegmenter.class));    
        lemmatizer = createEngine(createEngineDescription(StanfordLemmatizer.class));        
        posTagger = createEngine(createEngineDescription(OpenNlpPosTagger.class));  
        stemmer = createEngine(createEngineDescription(SnowballStemmer.class));              
    }

    private void setClearNlpComponents() 
            throws ResourceInitializationException {
        segmenter = createEngine(createEngineDescription(ClearNlpSegmenter.class));    
        lemmatizer = createEngine(createEngineDescription(StanfordLemmatizer.class));        
        posTagger = createEngine(createEngineDescription(ClearNlpPosTagger.class));  
        stemmer = createEngine(createEngineDescription(SnowballStemmer.class));      
    }            
    
}
