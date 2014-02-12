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
import hr.irb.zel.kpelab.extraction.esa.EsaSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.esa.EsamaxSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import java.util.List;

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
    public static void esaMaxCovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators
        EsamaxSearchPhraseSet phraseSet = new EsamaxSearchPhraseSet();
        KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);                
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);

        List<Phrase> result = tabuSearch.extract(doc);
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Metric metric = F1Evaluator.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7);
        
    }      

    // evaluate performance of esa extractor on single document
    public static void esaCosCovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators
        EsaSearchPhraseSet phraseSet = new EsaSearchPhraseSet(
                WordVectorMapFactory.getESAVectors());
        KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);                
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);

        List<Phrase> result = tabuSearch.extract(doc);
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Metric metric = F1Evaluator.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7);        
    }      
    
}
