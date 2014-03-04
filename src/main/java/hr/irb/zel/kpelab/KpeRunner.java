package hr.irb.zel.kpelab;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.morpha.MorphaStemmer;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import hr.irb.zel.kpelab.analysis.CannonizationAnalyser;
import hr.irb.zel.kpelab.analysis.EsaGraph;
import hr.irb.zel.kpelab.analysis.PhraseExtractionAnalyzer;
import hr.irb.zel.kpelab.analysis.PosTaggingAnalyser;
import hr.irb.zel.kpelab.analysis.WS353CorrelationAnalysis;
import hr.irb.zel.kpelab.analysis.devel.DevelTester;
import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemevalTests;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemevalUtils;
import hr.irb.zel.kpelab.corpus.semeval.DocumentCleaner;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.df.DfFactory;
import hr.irb.zel.kpelab.experiments.HulthCorpusExperiments;
import hr.irb.zel.kpelab.experiments.SemevalCorpusExperiments;
import hr.irb.zel.kpelab.experiments.SimilarityExperiments;
import hr.irb.zel.kpelab.vectors.input.WordToVectorMemMap;
import hr.irb.zel.kpelab.extraction.TfidfKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.VectorWordSimilarity;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.df.PhraseDocumentFrequency;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.DocAgg;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.PageRank;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.PhAgg;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.Vec;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.VecQ;
import static hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.create;
import hr.irb.zel.kpelab.util.REngineManager;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.util.VectorAggregator.Method;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.SparseRealVector;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.input.WordToVectorDiskMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity.SimilarityMeasure;
import hr.irb.zel.kpelab.vectors.document.TermPageRankVectorizer;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.uima.resource.ResourceInitializationException;
import org.tartarus.snowball.ext.PorterStemmer;

public class KpeRunner {

    public static void main(String[] args) throws Exception {
        start(); // init environment                              
        
        //extractionTests();
        //SimilarityExperiments.expWS353ESA();
        //CorpusSemevalTests.writePhraseLengths();        
        //DfFactory.createDfSemevalStemOpenNlp();        
        //singleDocGreedy();                                
        //esaGraph();
        
//        pageRankTests();                        
        develTest();               

        end(); // finalize environment
    }

    // 
    private static void pageRankTests() throws Exception {
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        IWordToVectorMap wvmSim = WordVectorMapFactory.getESA01Vectors();     
        //IVectorComparison vSim = new VectorSimilarity(SimilarityMeasure.COSINE);
        IVectorComparison vSim = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);
        CanonicForm cf = CanonicForm.STEM;
        TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlp();
        TermPageRankVectorizer pageRank = new TermPageRankVectorizer(wvm, wvmSim, vSim, cf, tdf, Method.SUM);        
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.AUTHOR);
        pageRank.vectorize(doc.getText());
    }
    
    private static void esaGraph() throws Exception {
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        VectorSimilarity vsim = new VectorSimilarity(SimilarityMeasure.COSINE);                
        EsaGraph eg = new EsaGraph(wvm, vsim);
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.AUTHOR);
        eg.processDocument(doc.getText(), doc.getId());
        eg.outputMatrix();
        eg.outputTerms();
    }   
    
    // run devel tests on a single extractor
    private static void develTest() throws Exception {
        DevelTester dt = new DevelTester(GreedyExtractorFactory.create(
                Vec.ESA, true, VectorMod.PRUNE, DocAgg.PRANK, 
                PageRank.SIM01EBE, Method.SUM, PhAgg.UW_SUM, VecQ.COS));            

        dt.init();
//        dt.testPhraseSets("basic", 5);
//        dt.testPhraseSets("mixed", 5);
//        dt.testPhraseSets("single", 5);
        dt.runOnSample(5, 10);        
        dt.close();
    }
    
    // run devel tests on a set of extractors
    private static void develTests() throws Exception {          
        for (GreedyExtractorConfig config : GreedyExtractorFactory.getAllCombinations()) {
            if (config != null)
            System.out.println(config.getId());
            DevelTester dt = new DevelTester(config);
            dt.init();
            dt.testPhraseSets("basic", 5);
            dt.testPhraseSets("mixed", 5);
            dt.testPhraseSets("single", 5);
            dt.runOnSample(5, 10);
            dt.runOnTrainSubsample(10);
            dt.close();        
        }
    }      
    
    private static void greedySubsample() throws Exception {        
        SemevalCorpusExperiments.greedyDatasetTrainSubsample(
                GreedyExtractorFactory.create(Vec.ESA, false, VectorMod.PRUNE, 
                DocAgg.TFIDF_SUM, null, null, PhAgg.UW_SUM, VecQ.COS), 10, 30);        
    }
    
    private static void extractionTests() throws Exception {
        PosExtractorConfig config = new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM);
        PosTaggingAnalyser posAnalyzer = new PosTaggingAnalyser(config);
        PosRegexPhraseExtractor phExtractor = new PosRegexPhraseExtractor(config);
        PhraseExtractionAnalyzer phAnalyzer = new PhraseExtractionAnalyzer(phExtractor);
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-35", SolutionPhraseSet.AUTHOR);
        phAnalyzer.printPhrases(doc);
        System.out.println("--------------");
        posAnalyzer.processDocument(doc);
        posAnalyzer.printProcessedDocument();
    }
    
    private static void coverageTests() throws Exception {
        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
                SolutionPhraseSet.AUTHOR);
        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
                SolutionPhraseSet.READER);        
        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
                SolutionPhraseSet.COBINED);          
    }
    
    private static void vec01Tests() throws Exception {
        IWordToVectorMap esa = WordVectorMapFactory.getESAVectors();
        IWordToVectorMap esa01 = WordVectorMapFactory.getESA01Vectors();
        System.out.println(esa.getWordVector("gener"));
        System.out.println(esa01.getWordVector("gener"));
    }
    
    private static void singleDocGreedy() throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.COBINED);        
        GreedyExtractorConfig conf = GreedyExtractorFactory.getESA01PrunedTfCosExtractor();        
        GreedyExtractor extr = new GreedyExtractor(10, conf);        
        List<Phrase> res = extr.extract(doc);
        System.out.println(res.size());        
    }
    
    public static void testCleaner() throws IOException {
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-43", SolutionPhraseSet.AUTHOR);
        //DocumentCleaner.analyzeDocument(doc.getText());        
        System.out.println(DocumentCleaner.cleanDocument(doc.getText()));
//        System.out.println(DocumentCleaner.isAbstract(" ABSTRACT  "));
//        System.out.println(DocumentCleaner.isAck(" ABSTRACT  "));
//        System.out.println(DocumentCleaner.isAppendix(" ABSTRACT  "));
//        System.out.println(DocumentCleaner.isRef(" ABSTRACT  "));
    }
    
    private static void cannonizationAnalysis() throws Exception {        
        CannonizationAnalyser ca = 
                new CannonizationAnalyser(CanonicForm.LEMMA, StanfordLemmatizer.class);
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-35", SolutionPhraseSet.AUTHOR);
        ca.printCanonicPhrases(doc);        
//        List<KpeDocument> docs = CorpusSemeval.getAll(SolutionPhraseSet.AUTHOR);
//        ca.testCanonicFormCoverage(WordVectorMapFactory.getLSIVectors(), docs);
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
    
    private static void testSubtract() {
        double [] v1 = {0,2,0,2,0,2},
                  v2 = {1,1,1,1,1,1};
        SparseRealVector sv1 = new SparseRealVector(v1), 
                         sv2 = new SparseRealVector(v2);
        IRealVector sv3 = sv1.clone();
        sv3.subtract(sv2);
        System.out.println(sv1);
        System.out.println(sv2);           
        System.out.println(sv3); 
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
        REngineManager.closeRengine();
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
        PhraseDocumentFrequency counter = new PhraseDocumentFrequency("counts_hulthAllPhrases_StanfordNlpPos");
        TfidfKpextractor extractor = new TfidfKpextractor(phExtractor, counter, 50);
        KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA)
                .readDocument("Training/639.abstr");
        List<Phrase> phrases = extractor.extract(doc);
        for (Phrase ph : phrases) {
            System.out.println(ph);
        }
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
         PhraseDocumentFrequency counter = 
                 new PhraseDocumentFrequency("counts_hulthAllPhrases_StanfordNlpPos");
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

