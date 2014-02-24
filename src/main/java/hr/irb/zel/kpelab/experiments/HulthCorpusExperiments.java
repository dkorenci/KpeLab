package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.CorpusHulth;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.coverage.phrase.IPhraseCoverage;
import hr.irb.zel.kpelab.coverage.phrase.MaxWordSimilarityCoverage;
import hr.irb.zel.kpelab.evaluation.F1Evaluator;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality.PhEquality;
import hr.irb.zel.kpelab.extraction.MaxCoverageExtractor;
import hr.irb.zel.kpelab.extraction.RandomPhraseExtractor;
import hr.irb.zel.kpelab.extraction.TfidfKpextractor;
import hr.irb.zel.kpelab.extraction.esa.EsaSearchPhraseSet;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.VectorWordSimilarity;
import hr.irb.zel.kpelab.tfidf.CounterCreation;
import hr.irb.zel.kpelab.tfidf.PhraseInDocumentsCounter;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import java.util.List;

/** Experiments with keyphrase extraction. */
public class HulthCorpusExperiments {

   // evaluate performance of tfidf extractor on single document
    public static void tfidfSingleDoc(String docName, int K) throws Exception {
         PhraseInDocumentsCounter counter = 
                 new PhraseInDocumentsCounter("counts_hulthAllPhrases_StanfordNlpPos");
         KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA).readDocument(docName);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         TfidfKpextractor tfidf = new TfidfKpextractor(extractor, counter, K);
         
         F1Evaluator eval = new F1Evaluator(tfidf, PhEquality.CANONIC);
         F1Metric metric = eval.evaluateDocument(doc);                
         System.out.println(metric);
         tfidf.printTfidfs();
    }

    // evaluate performance of tfidf extractor on single document
    public static void maxcovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators
        IWordSimilarityCalculator wordSim = new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(),
                new VectorSimilarity(VectorSimilarity.SimilarityMeasure.COSINE_CUTOFF));
        IPhraseCoverage phrCov = new MaxWordSimilarityCoverage(wordSim, false);
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA).readDocument(docName);
        PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        MaxCoverageExtractor kpextr = new MaxCoverageExtractor(extractor, phrCov, K);

        F1Evaluator eval = new F1Evaluator(kpextr, PhEquality.CANONIC);
        F1Metric metric = eval.evaluateDocument(doc);
        System.out.println(metric);
    }   

    // evaluate performance of esa extractor on single document
    public static void esacovSingleDoc(String docName, int K) throws Exception {
        // construct word and phrase similairty calculators

        EsaSearchPhraseSet phraseSet = new EsaSearchPhraseSet(WordVectorMapFactory.getESAVectors());
        KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);                
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.STEM).readDocument(docName);

        F1Evaluator eval = new F1Evaluator(tabuSearch, PhEquality.CANONIC);
        F1Metric metric = eval.evaluateDocument(doc);
        System.out.println(metric);
    }     
    
    /** Evaluate performance of tfidf extractor on the entire corpus. */    
    public static void tfidfCorpus(int K, String docFreqCounter) throws Exception {
         PhraseInDocumentsCounter counter = 
                 new PhraseInDocumentsCounter(docFreqCounter);         
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         TfidfKpextractor tfidf = new TfidfKpextractor(extractor, counter, K);
         
         List<KpeDocument> docs = CorpusHulth.getDocuments("Test", true, CanonicForm.LEMMA);
         
         F1Evaluator eval = new F1Evaluator(tfidf, PhEquality.CANONIC);
         F1Metric metric = eval.evaluateDocuments(docs);
         System.out.println(metric);         
    }        

    // evaluate performance of max. coverage extractor on the entire corpus
    public static void maxcovCorpus(int K) throws Exception {
        IWordSimilarityCalculator wordSim = new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(),
                new VectorSimilarity(VectorSimilarity.SimilarityMeasure.COSINE_CUTOFF));
        IPhraseCoverage phrCov = new MaxWordSimilarityCoverage(wordSim, false);        
        PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        MaxCoverageExtractor kpextr = new MaxCoverageExtractor(extractor, phrCov, K);
         
         List<KpeDocument> docs = CorpusHulth.getDocuments("Test", true, CanonicForm.LEMMA);
         
         F1Evaluator eval = new F1Evaluator(kpextr, PhEquality.CANONIC);
         F1Metric metric = eval.evaluateDocuments(docs);
         System.out.println(metric);         
    }     
    
    // evaluate performance of esa coverage extractor on the entire corpus
    public static void esacovCorpus(int K) throws Exception {
         EsaSearchPhraseSet phraseSet = new EsaSearchPhraseSet(WordVectorMapFactory.getESAVectors());
         KpeTabuSearch tabuSearch = new KpeTabuSearch(phraseSet, K);   
         System.out.print("reading documents... ");
         List<KpeDocument> docs = CorpusHulth.getDocuments("Test", true, CanonicForm.LEMMA);
         System.out.println("done.");
         F1Evaluator eval = new F1Evaluator(tabuSearch, PhEquality.CANONIC);
         F1Metric metric = eval.evaluateDocuments(docs);
         System.out.println(metric);         
    }       

    // evaluate performance of esa coverage extractor on the entire corpus
    public static void randomCorpus(int K) throws Exception {
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.STEM);
         RandomPhraseExtractor random = new RandomPhraseExtractor(extractor, K);
         System.out.print("reading documents... ");
         List<KpeDocument> docs = CorpusHulth.getDocuments("Test", true, CanonicForm.STEM);
         System.out.println("done.");
         F1Evaluator eval = new F1Evaluator(random, PhEquality.CANONIC);
         F1Metric metric = eval.evaluateDocuments(docs);
         System.out.println(metric);         
    }          
}
