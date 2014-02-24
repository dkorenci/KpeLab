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
import hr.irb.zel.kpelab.extraction.AllCandidatesExtractor;
import hr.irb.zel.kpelab.extraction.esa.EsaSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.esa.EsamaxSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.GreedyMaxExtractor;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.vectors.document.TermFrequencyVectorizer;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import java.util.List;
import org.apache.uima.resource.ResourceInitializationException;

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
    public static void esaMaxCovSingleDocGreedy(String docName, int K) throws Exception {        
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        GreedyMaxExtractor extractor = new GreedyMaxExtractor(K);
        
        List<Phrase> result = extractor.extract(doc);
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
    
    // test how many correct phrases (prom solution set phset) are covered 
    // by candidates generated by PosRegexPhraseExtractor using components comp
    public static void posRegexCoverage(Components comp, SolutionPhraseSet phset) 
            throws Exception {
        PosRegexPhraseExtractor phExtr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(comp, CanonicForm.STEM));         
        List<KpeDocument> docs = CorpusSemeval.getTest(phset);         
        AllCandidatesExtractor extr = new AllCandidatesExtractor(phExtr);
        F1Evaluator eval = new F1Evaluator(extr);
        F1Metric metric = eval.evaluateDocuments(docs);
        System.out.println(metric);          
    }    

    // evaluate performance of Greedy Extractor on a single semeval document
    public static void esaGreedySingleDoc(String docName, 
            GreedyExtractorConfig conf, int K) throws Exception {              
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        GreedyExtractor greedy = new GreedyExtractor(K, conf);        
        
        List<Phrase> result = greedy.extract(doc);
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Metric metric = F1Evaluator.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7);        
    }     
    
    public static void printTermFrequencies(String docName) throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        TermExtractor textr = new TermExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
        Utils.printWeightedTerms(textr.extractWeighted(doc.getText()));
    }
    
}
