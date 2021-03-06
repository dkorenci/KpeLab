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
import hr.irb.zel.kpelab.evaluation.IPhraseEquality;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality.PhEquality;
import hr.irb.zel.kpelab.extraction.AllCandidatesExtractor;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.esa.EsaSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.esa.EsamaxSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.GreedyMaxExtractor;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
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
        PhraseHelper.printPhraseSet(extractor.extractPhrases(doc.getText()), 4, false);
        System.out.println("*** solution keyphrases (combined) ***");
        PhraseHelper.printPhraseSet(doc.getKeyphrases(), 4, false);
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

        List<Phrase> result = tabuSearch.extract(doc.getText());
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Evaluator eval = new F1Evaluator(null, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7, false);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7, false);
        
    }      

    // evaluate performance of esa extractor on single document
    public static void esaMaxCovSingleDocGreedy(String docName, int K) throws Exception {        
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        GreedyMaxExtractor extractor = new GreedyMaxExtractor(K);
        
        List<Phrase> result = extractor.extract(doc.getText());
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Evaluator eval = new F1Evaluator(null, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7, false);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7, false);
        
    }      
    
    // evaluate performance of esa extractor on single document
    public static void esaCosCovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators
        EsaSearchPhraseSet phraseSet = new EsaSearchPhraseSet(
                WordVectorMapFactory.getESAVectors());
        KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);                
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);

        List<Phrase> result = tabuSearch.extract(doc.getText());
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Evaluator eval = new F1Evaluator(null, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7, false);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7, false);        
    }  
    
    // test how many correct phrases (prom solution set phset) are covered 
    // by candidates generated by PosRegexPhraseExtractor using components comp
    public static void posRegexCoverage(Components comp, SolutionPhraseSet phset) 
            throws Exception {
        PosRegexPhraseExtractor phExtr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(comp, CanonicForm.STEM));         
        List<KpeDocument> docs = CorpusSemeval.getTrain(phset);         
        AllCandidatesExtractor extr = new AllCandidatesExtractor(phExtr);
        F1Evaluator eval = new F1Evaluator(extr, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateDocuments(docs);
        System.out.println(metric);          
    }    
    
    // test how many correct phrases (prom solution set phset) are covered 
    // by candidates generated by given extractor using components comp
    public static void testCoverage(IPhraseExtractor phExtr, 
            SolutionPhraseSet phset) throws Exception {
        List<KpeDocument> docs = CorpusSemeval.getTrain(phset);         
        AllCandidatesExtractor extr = new AllCandidatesExtractor(phExtr);
        F1Evaluator eval = new F1Evaluator(extr, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateDocuments(docs);
        System.out.println(metric);          
    }      

    // evaluate performance of Greedy Extractor on a single semeval document
    public static void greedySingleDoc(String docName, 
            GreedyExtractorConfig conf, int K) throws Exception {              
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        GreedyExtractor greedy = new GreedyExtractor(K, conf);        
        
        List<Phrase> result = greedy.extract(doc.getText());
        List<Phrase> solution = doc.getKeyphrases();
        
        F1Evaluator eval = new F1Evaluator(null, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateResult(result, solution);
        System.out.println(metric);
        
        System.out.println("---- soultion");
        PhraseHelper.printPhraseSet(solution, 7, false);
        System.out.println("---- result");
        PhraseHelper.printPhraseSet(result, 7, false);        
    }     

    // evaluate performance of Greedy Extractor on a single subset of semeval documents
    public static void greedyDataset(String setId, 
            GreedyExtractorConfig conf, int K) throws Exception {        
        GreedyExtractor greedy = new GreedyExtractor(K, conf);    
        datasetExperiment(setId, greedy);
    }      
    
    // evalueate greedy extractor on a subsample of size S of train
    public static void greedyDatasetTrainSubsample(GreedyExtractorConfig conf, int K, int S) 
            throws Exception {
        GreedyExtractor greedy = new GreedyExtractor(K, conf);         
        trainSubsample(greedy, S);
    }     
    
    // evaluate performance of KpeExtractor on a single subset of semeval documents
    public static void datasetExperiment(String setId, 
            IKpextractor extractor) throws Exception {
        List<KpeDocument> docs = CorpusSemeval.getDataset(setId, SolutionPhraseSet.COBINED);       
        F1Evaluator eval = new F1Evaluator(extractor, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateDocuments(docs);
        System.out.println(metric);         
    }      
    
    // evaluate KpeExtractor on a subsample of size S of train
    public static F1Metric trainSubsample(IKpextractor extractor, int S) 
            throws Exception {
        List<KpeDocument> docs = CorpusSemeval.getDataset("train", SolutionPhraseSet.COBINED);
        List<KpeDocument> sample = Utils.getRandomSubsample(docs, S);
        //for (KpeDocument d : sample) System.out.println(d.getId());       
        F1Evaluator eval = new F1Evaluator(extractor, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateDocuments(sample);
        System.out.println(metric);         
        return metric;
    }         
    
    public static void printTermFrequencies(String docName) throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument(docName, SolutionPhraseSet.COBINED);
        TermExtractor textr = new TermExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
        Utils.printWeightedTerms(textr.extractWeighted(doc.getText()));
    }
    
}
