/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.analysis.PosTaggingAnalyser;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.evaluation.F1Evaluator;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.extraction.esa.EsaPhraseSet;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;

/**
 *
 */
public class SemevalCorpusExperiments {

    public static void singleDocOpenNLPPos() throws Exception {                                  
        PosExtractorConfig config =  
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM);
        KpeDocument doc = 
                CorpusSemeval.getDocument("train/H-37" , SolutionPhraseSet.COBINED);
        //PosTaggingAnalyser.analyseDocument(doc, config);
        PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(config);
        PhraseHelper.printPhraseSet(extractor.extractPhrases(doc.getText()), 4);
        System.out.println("*** solution keyphrases (combined) ***");
        PhraseHelper.printPhraseSet(doc.getKeyphrases(), 4);
    }        

    public static void singleDocExtraction() throws Exception {                                  
        PosExtractorConfig config =  
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM);
        KpeDocument doc = 
                CorpusSemeval.getDocument("train/H-37" , SolutionPhraseSet.COBINED);
        PosTaggingAnalyser.analyseDocument(doc, config);
    }           
    
    // evaluate performance of esa extractor on single document
    public static void esacovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators

        EsaPhraseSet phraseSet = new EsaPhraseSet(WordVectorMapFactory.getESAVectors());
        KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);                
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.STEM).readDocument(docName);

        F1Evaluator eval = new F1Evaluator(tabuSearch);
        F1Metric metric = eval.evaluateDocument(doc);
        System.out.println(metric);
    }      
    
}
