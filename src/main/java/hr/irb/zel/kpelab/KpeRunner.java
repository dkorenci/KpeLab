package hr.irb.zel.kpelab;

import hr.irb.zel.kpelab.analysis.PosTaggingAnalyser;
import hr.irb.zel.kpelab.analysis.WS353CorrelationAnalysis;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemevalTests;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemevalUtils;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.experiments.HulthCorpusExperiments;
import hr.irb.zel.kpelab.experiments.SemevalCorpusExperiments;
import hr.irb.zel.kpelab.experiments.SimilarityExperiments;
import hr.irb.zel.kpelab.vectors.input.WordToVectorMemMap;
import hr.irb.zel.kpelab.extraction.TfidfKpextractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.VectorWordSimilarity;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.tfidf.PhraseInDocumentsCounter;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.SparseRealVector;
import hr.irb.zel.kpelab.vectors.input.WordToVectorDiskMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import java.util.List;

public class KpeRunner {

    public static void main(String[] args) throws Exception {
        start(); // init environment                
        
        //testStemming();       
        //testWordStemming();
        //ExtractionExperiments.esacovCorpus(10);
        //ExtractionExperiments.randomCorpus(10);
        //ExtractionExperiments.esacovSingleDoc("Test/1933.abstr", 10);
        //ExtractionExperiments.esacovSingleDoc("Training/639.abstr", 10);        
        //esaVectorsTest();
        //printPhrases("Training/1078.abstr");
        //printPhrases("Training/639.abstr");
        //printPhrases("Training/90.abstr");
        //ws353vectorSims();
        //SemevalCorpusExperiments.singleDocOpenNLPPos();
        
        //SimilarityExperiments.expWS353ESA();
        //SimilarityExperiments.expWS353LSI();        
        //CorpusSemevalTests.printTerms("train/C-79");
        //SemevalCorpusExperiments.esaMaxCovSingleDoc("train/C-79", 10);
        //SemevalCorpusExperiments.esaCosCovSingleDoc("train/C-79", 10);
        //SemevalCorpusExperiments.esaMaxCovSingleDocGreedy("train/C-79", 10);
        //CorpusSemevalUtils.outputStemmedPhrases("devel/H-83");
        
//        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
//                SolutionPhraseSet.AUTHOR);
//        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
//                SolutionPhraseSet.READER);        
//        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
//                SolutionPhraseSet.COBINED);                       
        
        //CorpusSemevalTests.coverageErrors();
        
        System.out.println("porter2: " + PhraseHelper.stemWord("criterium"));
        System.out.println("porter: " + PhraseHelper.stemWordPorter("criterium"));
        
        end(); // finalize environment
    }

    private static void testMaxCoverage() {
        double [] v1 = {0,2,0,2,0,2},
                  v2 = {1,1,1,1,1,1};
        SparseRealVector sv1 = new SparseRealVector(v1), 
                         sv2 = new SparseRealVector(v2);
        System.out.println(sv1.sumMinShared(sv2));
        System.out.println(sv1.sumOfCoordinates());
        sv1.maxMerge(sv2);
        System.out.println(sv1);
    }
    
    private static void testSemevalCorpus() throws Exception {
        List<KpeDocument> docs = CorpusSemeval.getTest(SolutionPhraseSet.COBINED);
        for (KpeDocument doc : docs) {
            System.out.println(doc.getId());
            System.out.println("{{ " + doc.getText().substring(0,100) + " }}");
            String phrases = "";
            for (Phrase ph : doc.getKeyphrases()) phrases += (ph+" ; ");
            System.out.println(phrases);
        }
    }
    
    private static void ws353vectorSims() throws Exception {
        IWordSimilarityCalculator wordSimEsa =  new VectorWordSimilarity(
                WordVectorMapFactory.getESAVectors(), 
                new VectorSimilarity(VectorSimilarity.SimilarityMeasure.COSINE_CUTOFF));
        IWordSimilarityCalculator wordSimLsiCo =  new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(),
                new VectorSimilarity(VectorSimilarity.SimilarityMeasure.COSINE_CUTOFF));        
        IWordSimilarityCalculator wordSimLsiSc =  new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(),
                new VectorSimilarity(VectorSimilarity.SimilarityMeasure.COSINE_SCALED));              
        boolean stem = false;
        WS353CorrelationAnalysis.outputSimilarityTable("ws353lsi_cosscaled.txt", wordSimLsiSc, stem);
    }
    
    private static void esaVectorsTest() throws Exception {
        WordToVectorDiskMap wvec = WordVectorMapFactory.getESAVectors();
        //String [] words = {"comput", "car", "bride"};
        String [] words = {"stock", "applic"};
        IRealVector [] vec = new IRealVector[words.length];
        IRealVector sum = null;
        for (int i = 0; i < words.length; ++i) {
            vec[i] = wvec.getWordVector(words[i]);            
            if (sum == null) sum = vec[i];
            else sum.add(vec[i]);
            System.out.println(vec[i]);
        }
        System.out.println(sum);
    }   

    private static void sparseExperiments() {
        double [] v1 = {0,1,2,3,4,5,6};
        double [] v2 = {0,1,2,3,4,5,6};       
        SparseRealVector sv1 = new SparseRealVector(10);
        SparseRealVector sv2 = new SparseRealVector(10);
        sv1.setElement(0, 1);
        sv1.setElement(2, 1);
        sv1.setElement(4, 1);
        sv1.setElement(5, 2);
        
        //sv2.setElement(1, 1);
        sv2.setElement(2, 1);
        sv2.setElement(8, 1);
        sv2.setElement(6, 2);
        
        System.out.println(sv1.cosine(sv2));
    }
    
    // finalization and cleanup
    private static void end() throws Exception {
        WordVectorMapFactory.closeFactory();
    }
    
    // initialization
    private static void start() {}

    
    public static void testWordStemming() throws Exception {
        System.out.println(PhraseHelper.stemWord("science"));
        System.out.println(PhraseHelper.stemWord("scientific"));
        System.out.println(PhraseHelper.stemWord("scientist"));
    }
    
    public static void testStemming() throws Exception {
        PosRegexPhraseExtractor phExtractor = new PosRegexPhraseExtractor(CanonicForm.STEM);
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.STEM)
                .readDocument("Training/639.abstr");
        List<Phrase> phrases = phExtractor.extractPhrases(doc.getText());
        for (Phrase ph : phrases) {
            System.out.println(ph + " : " + ph.canonicForm());
        }
    }    
    
    public static void printTopTfidfPhrases() throws Exception {
        PosRegexPhraseExtractor phExtractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        PhraseInDocumentsCounter counter = new PhraseInDocumentsCounter("counts_hulthAllPhrases_StanfordNlpPos");
        TfidfKpextractor extractor = new TfidfKpextractor(phExtractor, counter, 50);
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA)
                .readDocument("Training/639.abstr");
        List<Phrase> phrases = extractor.extract(doc);
        for (Phrase ph : phrases) {
            System.out.println(ph);
        }
    }
    
    public static void testVectors() throws Exception {
        WordToVectorMemMap wvf = new WordToVectorMemMap(
                "/data/datasets/word_vectors/senna3.0_embeddings/words.lst", 
                "/data/datasets/word_vectors/senna3.0_embeddings/embeddings.txt");
    }    

    public static void testDiskVectors() throws Exception {
        WordToVectorDiskMap wvf = new WordToVectorDiskMap(
                "/data/datasets/word_vectors/wiki_lsi/wiki-words.txt", 
                "/data/datasets/word_vectors/wiki_lsi/wiki-matrix.txt", true, false);
        wvf.getWordVector("car");
        wvf.getWordVector("cat");
    }       
    
    public static void printPhrases(String docFile) throws Exception {
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA)
                .readDocument(docFile);
        PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        List<Phrase> phrases = extractor.extractPhrases(doc.getText());
        List<Phrase> solution = doc.getKeyphrases();
        System.out.println("candidate phrases: ");
        PhraseHelper.printPhraseSet(phrases, 5);
        System.out.println("solution phrases: ");
        PhraseHelper.printPhraseSet(solution, 5);        
    }
    
     public static void testTfIdf() throws Exception {                  
         PhraseInDocumentsCounter counter = 
                 new PhraseInDocumentsCounter("counts_hulthAllPhrases_StanfordNlpPos");
         KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA)
                 .readDocument("Training/1037.abstr");
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         TfidfKpextractor tfidf = new TfidfKpextractor(extractor, counter, 20);
         tfidf.extract(doc);
     }
            
    public static void testWordCanonization() throws Exception {
        //KpeDocument doc = DocumentReaderHulth.readDocument("Training/1044.abstr");
        //System.out.println(doc.getText());
        List<PhraseHelper.TokenCanonic> canonic = PhraseHelper.getCanonicForms(
                "Moore Rayleigh's    one     two three all monkeys go went"
                + " take took underscore multiple multiply", CanonicForm.STEM);
        for (PhraseHelper.TokenCanonic tl : canonic) System.out.print(tl.token+","+tl.canonic+"|");
        System.out.println("");
    }   
}

