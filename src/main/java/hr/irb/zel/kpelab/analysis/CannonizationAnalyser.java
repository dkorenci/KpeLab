/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.analysis;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Tests for lemmatization and stemming.
 */
public class CannonizationAnalyser {

    // class of the cannonizer (stemmer or lemmatizator) component
    private final Class<? extends AnalysisComponent> cannonClass;
    PosExtractorConfig extrConfig;
    PosRegexPhraseExtractor extractor;
    CanonicForm canonicForm;
    
    public CannonizationAnalyser(CanonicForm cform, Class<? extends AnalysisComponent> cc) {
        cannonClass = cc; canonicForm = cform;
    }
    
    private void initializeExtractor() throws ResourceInitializationException {
        if (extractor != null) return;
        extrConfig = new PosExtractorConfig(Components.OPEN_NLP, canonicForm);
        if (canonicForm == CanonicForm.LEMMA)
            extrConfig.lemmatizer = createEngine(createEngineDescription(cannonClass)); 
        else if (canonicForm == CanonicForm.STEM)
            extrConfig.stemmer = createEngine(createEngineDescription(cannonClass));
        else if (canonicForm != CanonicForm.NO_CANNONIZATION)                         
            throw new IllegalArgumentException("uncovered canonic form");
        extractor = new PosRegexPhraseExtractor(extrConfig);
    }

    // print canonic forms of phrases extracted by PosRegexKeyphraseExtractor
    public void printCanonicPhrases(KpeDocument doc) throws UIMAException {
        initializeExtractor();
        List<Phrase> phrases = extractor.extractPhrases(doc.getText());
        for (Phrase ph : phrases) {
            System.out.println(ph.toString() + " : " + ph.canonicForm());
        }
    }
    
    // test for a document collection how well are canonic forms 
    // of phrases extracted by PosRegexKeyphraseExtractor covered by
    // (existent in) a WordToVectorMap
    public void testCanonicFormCoverage(IWordToVectorMap wvmap, List<KpeDocument> docs) 
            throws UIMAException {
        initializeExtractor();
        double microAverage = -1; 
        int coveredTokens = 0, totalTokens = 0;
        for (KpeDocument doc : docs) {
            int docCovered = 0, docTotal = 0;
            List<Phrase> phrases = extractor.extractPhrases(doc.getText());            
            for (Phrase ph : phrases)
            for (String tok : ph.getCanonicTokens())
            if (Utils.isWord(tok)) {
                totalTokens++; docTotal++;
                if (wvmap.hasWord(tok)) { 
                    coveredTokens++;
                    docCovered++;
                }
            }
            double docAvg = (double)docCovered/docTotal;
            if (microAverage == -1) microAverage = docAvg;
            else microAverage += docAvg;
        }
        microAverage /= docs.size();
        double macroAverage = (double)coveredTokens/totalTokens;   
        System.out.println("microAverage: " + Utils.doubleToString(microAverage));
        System.out.println("macroAverage: " + Utils.doubleToString(macroAverage));
        System.out.println("covered tokens: " + coveredTokens + " , " 
                + "total tokens: " + totalTokens);        
    }
    
}
