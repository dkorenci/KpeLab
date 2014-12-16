package hr.irb.zel.kpelab;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import hr.irb.zel.kpelab.analysis.CannonizationAnalyser;
import hr.irb.zel.kpelab.analysis.EsaGraph;
import hr.irb.zel.kpelab.analysis.PhraseExtractionAnalyzer;
import hr.irb.zel.kpelab.analysis.PosTaggingAnalyser;
import hr.irb.zel.kpelab.analysis.VectorAnalyser;
import hr.irb.zel.kpelab.analysis.WS353CorrelationAnalysis;
import hr.irb.zel.kpelab.analysis.devel.DevelTester;
import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.DocumentCleaner;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.df.DfFactory;
import hr.irb.zel.kpelab.experiments.SemevalCorpusExperiments;
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
import hr.irb.zel.kpelab.df.PhraseDocumentFrequency;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.extraction.AllCandidatesExtractor;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.DocAgg;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.PhAgg;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.Vec;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorFactory.VecQ;
import hr.irb.zel.kpelab.extraction.greedy.WGreedyExtractor;
import hr.irb.zel.kpelab.extraction.ranking.KpMinerExtractor;
import hr.irb.zel.kpelab.extraction.ranking.RankerExtractor;
import hr.irb.zel.kpelab.inspector.KpeInspector;
import hr.irb.zel.kpelab.phrase.FirstOccurenceExtractor;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.IPhraseScore;
import hr.irb.zel.kpelab.util.REngineManager;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.util.VectorAggregator.Method;
import vectors.IRealVector;
import vectors.SparseRealVector;
import vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.input.WordToVectorDiskMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import vectors.comparison.VectorSimilarity;
import vectors.comparison.VectorSimilarity.SimilarityMeasure;
import hr.irb.zel.kpelab.vectors.document.TermPageRankVectorizer;
import hr.irb.zel.kpelab.vectors.document.TermPageRankVectorizer.SimMod;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class KpeRunner {

    public static void main(String[] args) throws Exception {
        start(); // init environment                              
        
        //extractionTests();
        //SimilarityExperiments.expWS353ESA();
        //CorpusSemevalTests.writePhraseLengths();        
        //DfFactory.createDfSemevalStemOpenNlp();        
        //singleDocGreedy();                                
        //esaGraph();
                
        //pageRankTests();                        
                      
                    
//        SemevalCorpusExperiments.posRegexCoverage(Components.OPEN_NLP, 
//                SolutionPhraseSet.READER);

        //semevalCoverage();
        //develTest();
        //DfFactory.createDfHulthStemOpenNlp();
        //HulthCorpusExperiments.greedyCorpus(10);
        //testCandidates();    
        //pageRankTests();
        //verboseGreedy();
        //kpminerGrid();
        
        //wcoverageExperiment();        
        //phsumExperiment();
        //inspect();        
        //rankerExperiment();        
//        DfFactory.createDfSemevalStemOpenNlpTrainTrial();
//        DfFactory.createDfSemevalStemOpenNlpAll();
        
        //CorpusSemeval.getTest(SolutionPhraseSet.COBINED);
        
        VectorAnalyser.analyse(WordVectorMapFactory.getESAVectors());
        
        end(); // finalize environment
    }

    private static void phsumExperiment() throws Exception {
        GreedyExtractorConfig conf = GreedyExtractorFactory.
                create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
                    null, 0, null, null, PhAgg.PH_SUM, VecQ.COS);   
        
        //IKpextractor extr = new GreedyExtractor(10, conf);
        
        //IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfHulthStemOpenNlp(), 0);                 
        IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfSemevalStemOpenNlpAll(), 0);                         
        IKpextractor extr = new WGreedyExtractor(15, conf, scr);                
        SemevalCorpusExperiments.trainSubsample(extr, 20);
        //SemevalCorpusExperiments.datasetExperiment("test", extr);
        //HulthCorpusExperiments.testCorpus(extr, CanonicForm.STEM);
    }        
    
    private static void wcoverageExperiment() throws Exception {
        GreedyExtractorConfig conf = GreedyExtractorFactory.
                create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
                    null, 0, null, null, PhAgg.UW_SUM, VecQ.COS);   
        //IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfHulthStemOpenNlp(), 0); 
        IPhraseScore scr = new KpMinerExtractor(conf.phraseExtractor, DfFactory.loadDfSemevalStemOpenNlpAll(), 0);
        
//        IKpextractor extr = new GreedyExtractor(15, conf);
        
        IKpextractor extr = new WGreedyExtractor(15, conf, scr);        
        //HulthCorpusExperiments.testCorpus(extr, CanonicForm.STEM);
        SemevalCorpusExperiments.trainSubsample(extr, 20);
        //SemevalCorpusExperiments.datasetExperiment("test", extr);
    }    
    
    private static void kpminerGrid() throws Exception {
        double [] ss = { 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7, 1.9, 2.1 };
        double [] tt = { 2, 2.2, 2.4, 2.6, 2.8, 3, 3.2, 3.4, 3.6, 3.8, 4, 4.2, 4.4 };

        IPhraseExtractor extr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));         
        
        double maxf1 = Double.NEGATIVE_INFINITY, maxs = 0, maxt = 0;
        for (double s : ss) {
            for (double t : tt) {
                IKpextractor kpextr = new KpMinerExtractor(extr, 
                        DfFactory.loadDfSemevalStemOpenNlpAll(), 15, s, t);    
        //        IKpextractor kpextr = new RankerExtractor(extr, 
        //                DfFactory.loadDfSemevalStemOpenNlp(), 15);      
                System.out.println("s: " + s + " t: " + t);
                F1Metric m = SemevalCorpusExperiments.trainSubsample(kpextr, 50);                
                if (m.f1 > maxf1) {
                    maxf1 = m.f1; maxs = s; maxt = t;
                }
            }
        }
        
        System.out.println("max f1: " + maxf1);
        System.out.println("for s: " + maxs + " t: " + maxt);        
    }    
    
    private static void rankerExperiment() throws Exception {
        IPhraseExtractor extr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));        
        IKpextractor kpextr = new RankerExtractor(extr, DfFactory.loadDfSemevalStemOpenNlpAll(), 15);
                //DfFactory.loadDfSemevalStemOpenNlpTrainTrial(), 15);                  
//        IPhraseExtractor extr = new NgramPhraseExtractor(
//                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));         
//        IKpextractor kpextr = new KpMinerExtractor(extr,DfFactory.loadDfSemevalStemOpenNlpTrainTrial(), 15);    
        //SemevalCorpusExperiments.trainSubsample(kpextr, 20);
        SemevalCorpusExperiments.datasetExperiment("train", kpextr);
        //HulthCorpusExperiments.testCorpus(kpextr, CanonicForm.STEM);
    }
    
    private static void inspect() throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument("train/J-34", SolutionPhraseSet.COBINED);        
        KpeInspector inspector = new KpeInspector(doc);
        
//        GreedyExtractorConfig conf = GreedyExtractorFactory.
//                create(Vec.ESA, true, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
//                    null, 0, null, null, PhAgg.UW_SUM, VecQ.COS);        
        
        IPhraseExtractor posextr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));                
        
//        IPhraseExtractor extr = new NgramPhraseExtractor(
//                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));        
        IKpextractor allextr = new AllCandidatesExtractor(posextr);
        
//        IKpextractor kpextr = new KpMinerExtractor(extr, 
//                DfFactory.loadDfSemevalStemOpenNlp(), 10, 1, 3);
//        IKpextractor kpextr = new RankerExtractor(extr, 
//                DfFactory.loadDfSemevalStemOpenNlp(), 15);    
        
//        GreedyExtractorConfig conf = GreedyExtractorFactory.
//                create(Vec.ESA, true, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
//                    null, 0, null, null, PhAgg.UW_SUM, VecQ.COS);     

          GreedyExtractorConfig conf = GreedyExtractorFactory.
                create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
                    null, 0, null, null, PhAgg.UW_SUM, VecQ.COS);   
        IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfSemevalStemOpenNlpAll(), 0);         
        
//        IKpextractor extr = new GreedyExtractor(15, conf);
        
        IKpextractor extr = new WGreedyExtractor(15, conf, scr);          
        
        inspector.extractedPhrases(extr);
        inspector.phrasesByFrequency(posextr);
        //inspector.phrasesByFrequency(extr);
//        List<Phrase> kphrases = kpextr.extract(doc);
//        List<Phrase> phrases100 = (new RankerExtractor(extr, 
//                DfFactory.loadDfSemevalStemOpenNlp(), 100)).extract(doc);
//        phrases100.removeAll(kphrases);
//        inspector.coverage(phrases100, conf, 2);
//        inspector.coverage(phrases100, conf, 3);
    }    
    
    private static void semevalCoverage() throws Exception {
        IPhraseExtractor extr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
        double [] perc = { 0.1, 0.2, 0.3 };
        SolutionPhraseSet solPhSet = SolutionPhraseSet.READER;
        for (double p : perc) {
            IPhraseExtractor filter = new FirstOccurenceExtractor(extr, p);
            System.out.println("*** percentage: " + p);
            SemevalCorpusExperiments.testCoverage(filter, solPhSet);
        }        
    }
    
    // tests extraction of candidates
    private static void testCandidates() throws Exception {
        IPhraseExtractor extr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
        List<KpeDocument> docs = CorpusSemeval.getDataset("devel", SolutionPhraseSet.AUTHOR);
//        List<KpeDocument> docs = new ArrayList<KpeDocument>();
//        docs.add(CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.AUTHOR));
        for (KpeDocument doc : docs) {
            BufferedWriter w = new BufferedWriter(
                    new FileWriter(KpeConfig.getProperty("devel.tests")+doc.getId()+".cand.txt"));
            List<Phrase> phrases = extr.extractPhrases(doc.getText());            
            for (Phrase ph : phrases) {
                w.write(Utils.fixw(Integer.toString(ph.getFirstOccurence()),6)); 
                w.write(Utils.fixw(Integer.toString(ph.getFrequency()),6)); 
                w.write(" " + ph.toString() + " ; " + ph.canonicForm());
                w.write("\n");
            }
            w.close();
        }         
    }
    
    private static void verboseGreedy() throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.COBINED);        
        GreedyExtractorConfig conf = GreedyExtractorFactory.
                create(Vec.ESA, true, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
                    null, 0, null, null, PhAgg.UW_MAX, VecQ.EBE);        
        GreedyExtractor extr = new GreedyExtractor(10, conf);        
        String outFolder = KpeConfig.getProperty("devel.tests") + "measure/";
        extr.makeVerbose(outFolder);
        extr.extract(doc.getText());        
    }
    
    // 
    private static void pageRankTests() throws Exception {
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        IWordToVectorMap wvmSim = WordVectorMapFactory.getESAVectors();     
        //IWordToVectorMap wvmSim = WordVectorMapFactory.getESA01Vectors();     
        IVectorComparison vSim = new VectorSimilarity(SimilarityMeasure.COSINE);
        //IVectorComparison vSim = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);
        CanonicForm cf = CanonicForm.STEM;
        TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlpAll();
        TermPageRankVectorizer pageRank = new TermPageRankVectorizer(wvm, wvmSim, 
                vSim, cf, tdf, Method.SUM, 0.85, SimMod.NONE);        
        KpeDocument doc = CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.AUTHOR);
        System.out.println(pageRank.getId());
        pageRank.print(doc.getText());
                
//        List<KpeDocument> docs = new ArrayList<KpeDocument>();
//        List<KpeDocument> docs = CorpusSemeval.getDataset("devel", SolutionPhraseSet.AUTHOR);
//        docs.add(CorpusSemeval.getDocument("devel/H-83", SolutionPhraseSet.AUTHOR));        
//        for (KpeDocument doc : docs) {            
//            BufferedWriter w = new BufferedWriter(
//                    new FileWriter(KpeConfig.getProperty("devel.tests")+doc.getId()+".prank.txt"));
//            List<WeightedTerm> prank = pageRank.getRanks(doc.getText());
//            Collections.sort(prank);
//            for (WeightedTerm t : prank) {
//                w.write(Utils.fixw(t.term, 15) + Utils.fixw(Utils.doubleStr(t.weight, 10),15)); 
//                w.write("\n");
//            }
//            w.close();
//        }        
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
                Vec.ESA, true, VectorMod.PRUNE, DocAgg.TFIDF_SUM, 
                null, -1, null , null, PhAgg.UW_SUM, VecQ.COS));            
        dt.init();
//        dt.testPhraseSets("basic", 5);
//        dt.testPhraseSets("mixed", 5);
//        dt.testPhraseSets("single", 5);
//        dt.runOnSample(5, 10);        
        dt.runOnTrainSubsample(10);
        dt.close();
    }
    
    // run devel tests on a set of extractors
    private static void develTests() throws Exception {          
        for (GreedyExtractorConfig config : GreedyExtractorFactory.getPrankCombinations()) {
            System.out.println(config.getId());
//            DevelTester dt = new DevelTester(config);
//            dt.init();
//            dt.testPhraseSets("basic", 5);
//            dt.testPhraseSets("mixed", 5);
//            dt.testPhraseSets("single", 5);
//            dt.runOnSample(5, 10);
//            dt.runOnTrainSubsample(10);
//            dt.close();        
        }
    }      
    
    private static void greedySubsample() throws Exception {        
        SemevalCorpusExperiments.greedyDatasetTrainSubsample(
                GreedyExtractorFactory.create(Vec.ESA, false, VectorMod.PRUNE, 
                DocAgg.TFIDF_SUM, null, 0, null, null, PhAgg.UW_SUM, VecQ.COS), 10, 30);        
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
        List<Phrase> res = extr.extract(doc.getText());
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
        List<Phrase> phrases = extractor.extract(doc.getText());
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
        PhraseHelper.printPhraseSet(phrases, 5, false);
        System.out.println("solution phrases: ");
        PhraseHelper.printPhraseSet(solution, 5, false);        
    }
    
     public static void testTfIdf() throws Exception {                  
         PhraseDocumentFrequency counter = 
                 new PhraseDocumentFrequency("counts_hulthAllPhrases_StanfordNlpPos");
         KpeDocument doc = new DocumentReaderHulth(true, CanonicForm.LEMMA)
                 .readDocument("Training/1037.abstr");
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         TfidfKpextractor tfidf = new TfidfKpextractor(extractor, counter, 20);
         tfidf.extract(doc.getText());
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

