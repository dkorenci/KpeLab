package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.df.DfFactory;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.greedy.phrase.MaxPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.greedy.phrase.PhSumPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.greedy.phrase.SumPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.greedy.phrase.WSumPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.ranking.RankerExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.FirstOccurenceExtractor;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.IPhraseScore;
import hr.irb.zel.kpelab.phrase.NgramPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.SubphraseRemover;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.util.VectorAggregator.Method;
import vectors.comparison.IVectorComparison;
import vectors.comparison.VectorSimilarity;
import vectors.comparison.VectorSimilarity.SimilarityMeasure;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import hr.irb.zel.kpelab.vectors.document.TermFrequencyVectorizer;
import hr.irb.zel.kpelab.vectors.document.TermPageRankVectorizer;
import hr.irb.zel.kpelab.vectors.document.TermPageRankVectorizer.SimMod;
import hr.irb.zel.kpelab.vectors.document.TfIdfVectorizer;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import java.util.ArrayList;
import java.util.List;

/** Methods for creating various GreedExtractor configurations. */
public class GreedyExtractorFactory {

//    public GreedyExtractorConfig(IDocumentVectorizer dvec, IPhraseExtractor phext, 
//            IPhraseSetVectorizer phvec, IVectorComparison cmp) {    
    
    public static GreedyExtractorConfig[] getAllExtractors() throws Exception {
        GreedyExtractorConfig[] exts = {
            getLSICosExtractor(), 
            getESATfCosExtractor(),getESA01TfCosExtractor(),
            getESA01TfEbeExtractor(), getESA01TfIdfCosExtractor(), 
            getESAPrunedTfCosExtractor(), getESA01PrunedTfCosExtractor()
        };
        return exts;
    }

//    public static GreedyExtractorConfig create(Vec vec, boolean vec01, VectorMod vecMod, 
//            DocAgg doc, PageRank pr, Method aggMeth, PhAgg ph, VecQ vecq) throws Exception {
        
    // get combinations of various esa vectors all with tfidf-sum, uw-sum and cos
    public static GreedyExtractorConfig[] getCombinations() throws Exception {
        GreedyExtractorConfig[] exts = {
    //        create(Vec.ESA, false, VectorMod.NONE, DocAgg.TFIDF_MAX, PhAgg.UW_MAX, VecQ.COS),
    //        create(Vec.ESA, true, VectorMod.NONE, DocAgg.TFIDF_MAX, PhAgg.UW_MAX, VecQ.COS),
//            create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.PRANK, 
//                PageRank.SIMCOS , Method.SUM, PhAgg.UW_SUM, VecQ.COS),            
//            create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.PRANK, 
//                PageRank.SIMCOS , Method.MAX, PhAgg.UW_SUM, VecQ.COS),            
//            create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.PRANK, 
//                PageRank.SIM01EBE , Method.SUM, PhAgg.UW_SUM, VecQ.COS),                        
//            create(Vec.ESA, false, VectorMod.PRUNE, DocAgg.PRANK, 
//                PageRank.SIM01EBE , Method.MAX, PhAgg.UW_SUM, VecQ.COS),                                    
        };
        return exts;
    }    
    
    // return a list of all reasonable combinations
    public static List<GreedyExtractorConfig> getAllCombinations() throws Exception {
        List<GreedyExtractorConfig> combinations = new ArrayList<GreedyExtractorConfig>();
        // options that showed more promise go first
        Vec [] vec = { Vec.ESA };
        boolean [] vec01 = {false, true};
        // first prune modes, since they run quicker
        VectorMod [] vecMod = { VectorMod.PRUNE, VectorMod.NONE };
        DocAgg [] doc = { DocAgg.TFIDF_SUM, DocAgg.TFIDF_MAX, DocAgg.TF_SUM };
        PageRank [] prank = { PageRank.SIMCOS, PageRank.SIM01EBE };
        SimMod [] prSmod = { SimMod.NONE, SimMod.EXP };
        double [] prDf = {0.7, 0.85};
        Method [] method = { Method.SUM, Method.MAX };
        PhAgg [] ph = { PhAgg.UW_SUM, PhAgg.UW_SUM };
        VecQ [] vecq = { VecQ.COS, VecQ.EBE };
        
//    public static GreedyExtractorConfig create(
//            Vec vec, boolean vec01, VectorMod vecMod, DocAgg doc, 
//            PageRank pr, double prDf, SimMod prSm, 
//            Method aggMeth, PhAgg ph, VecQ vecq) throws Exception {

        
        for (Vec v : vec) {
            for (boolean v01 : vec01) {
                for (VectorMod vm : vecMod) {
                    for (DocAgg d : doc) {
                        if (d != DocAgg.PRANK) {
                            for (PhAgg p : ph)
                            for (VecQ vq : vecq) {                                
                                GreedyExtractorConfig conf = 
                                        create(v, v01, vm, d, null, 0.0, null , null, p, vq);
                                combinations.add(conf);
                            }                            
                        }
                        else {
                            for (PageRank pr : prank) {
                                for (double df : prDf) {
                                for (SimMod sm : prSmod) {                                
                                for (Method m : method) {
                                    for (PhAgg p : ph) {
                                        for (VecQ vq : vecq) {                                
                                            GreedyExtractorConfig conf = 
                                                    create(v, v01, vm, d, pr ,df, sm ,m, p, vq);
                                            combinations.add(conf);
                                        }
                                    }                                    
                                }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return combinations;
    }      

    // return a list of all reasonable combinations
    public static List<GreedyExtractorConfig> getPrankCombinations() throws Exception {
        List<GreedyExtractorConfig> combinations = new ArrayList<GreedyExtractorConfig>();
        // options that showed more promise go first
        Vec [] vec = { Vec.ESA };
        boolean [] vec01 = {false, true}; // 2
        // only pruned vectors!
        VectorMod [] vecMod = { VectorMod.PRUNE }; // 2
        DocAgg [] doc = { DocAgg.PRANK }; // 1
        PageRank [] prank = { PageRank.SIMCOS, PageRank.SIM01EBE }; // 2
        SimMod [] prSmod = { SimMod.NONE, SimMod.EXP }; // 2
        double [] prDf = {0.7, 0.85}; // 2
        Method [] method = { Method.SUM, Method.MAX }; // 2
        PhAgg [] ph = { PhAgg.UW_SUM, PhAgg.UW_SUM }; // 2
        VecQ [] vecq = { VecQ.COS, VecQ.EBE }; // 2
        // 128 combinations
        
//    public static GreedyExtractorConfig create(
//            Vec vec, boolean vec01, VectorMod vecMod, DocAgg doc, 
//            PageRank pr, double prDf, SimMod prSm, 
//            Method aggMeth, PhAgg ph, VecQ vecq) throws Exception {

        
        for (Vec v : vec) {
            for (boolean v01 : vec01) {
                for (VectorMod vm : vecMod) {
                    for (DocAgg d : doc) {
                        for (PageRank pr : prank) {
                            for (double df : prDf) {
                            for (SimMod sm : prSmod) {                                
                            for (Method m : method) {
                                for (PhAgg p : ph) {
                                    for (VecQ vq : vecq) {                                
                                        GreedyExtractorConfig conf = 
                                                create(v, v01, vm, d, pr ,df, sm ,m, p, vq);
                                        combinations.add(conf);
                                    }
                                }                                    
                            }
                            }
                            }
                        }                        
                    }
                }
            }
        }
        
        return combinations;
    }          
    
    // options for configuration creation
    public enum Vec { LSI, ESA }
    public enum VecMod {
        NONE, // no modification
        ZERO_ONE, // turn non zero coordinates to 1
        PRUNE // prune unshared coordinates in a document
    }
    // document to vector aggregation method
    public enum DocAgg {
        TF_SUM, // sum content words mult. by tf
        TFIDF_SUM, // sum content words mult. by tfidf
        TFIDF_MAX, // coordinatewise max. of words mult. by tfidf
        PRANK // aggregate content words mult. by pagerank        
    }    
    // pagerank aggregation options
    public enum PageRank {
        SIM01EBE, // similarity using esa01 + ebeMultiply (number of common coordinates)
        SIMCOS // similarity using esa + cosine
    }    
    // phrase set to vector aggregation method
    public enum PhAgg { 
        UW_SUM, // sum of vectors of unique words
        UW_MAX, // max of vectors of unique words
        UW_WEIGHTED, // sum of vectors of unique words, weighted with phrase weights
        PH_SUM, // sum of phrases
        PH_SUM_WEIGHTED // sum of weighted phrases
    }
    // quality measure that compares phrase set and document vectors
    public enum VecQ { COS, COS_CUT, EBE }
            
    // creates extractor config based on options
    // argume aggMeth is aggregation method, valid for page rank
    public static GreedyExtractorConfig create(
            Vec vec, boolean vec01, VectorMod vecMod, DocAgg doc, 
            PageRank pr, double prDf, SimMod prSm, 
            Method aggMeth, PhAgg ph, VecQ vecq) throws Exception {
        // canonic form
        CanonicForm cform;
        if (vec == Vec.LSI) cform = CanonicForm.LEMMA;
        else if (vec == Vec.ESA) cform  = CanonicForm.STEM;
        else throw new UnsupportedOperationException(); 
        
        // phrase extractor
//        IPhraseExtractor phext = new FirstOccurenceExtractor(new PosRegexPhraseExtractor(
//                new PosExtractorConfig(Components.OPEN_NLP, cform)), 0.2);    
        IPhraseExtractor phext =  new SubphraseRemover(new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform)));    
//        IPhraseExtractor phext =  new SubphraseRemover(new NgramPhraseExtractor(
//                new PosExtractorConfig(Components.OPEN_NLP, cform)));    
        
                
        // word to vector mapping
        IWordToVectorMap wvm;
        if (vec == Vec.LSI) {
            wvm = WordVectorMapFactory.getLSIVectors();
        }
        else if (vec == Vec.ESA) {
            if (vec01) wvm = WordVectorMapFactory.getESA01Vectors();
            else wvm = WordVectorMapFactory.getESAVectors();
            
        }
        else throw new UnsupportedOperationException();         
        // "local Wvm", for doc aggregation and phrase aggregation components
        // set to null if vector prunning is on, to capture potential bugs
        // with initialization, since word to vector maps for these components
        // have to be initialized before each document processing        
        IWordToVectorMap compWvm;
        if (vecMod == VectorMod.PRUNE || vecMod == VectorMod.PRUNE_ADD_UNIQUE) compWvm = null; 
        else compWvm = wvm;        
        
        // document vectorization
        IDocumentVectorizer dvec;
        if (doc == DocAgg.TFIDF_SUM || doc == DocAgg.TFIDF_MAX) {   
            TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlpAll();
            Method m; // aggregation method
            if (doc == DocAgg.TFIDF_SUM) m = Method.SUM; else m = Method.MAX;
            dvec = new TfIdfVectorizer(compWvm, cform, tdf, m);
        }
        else if (doc == DocAgg.PRANK) {            
            IWordToVectorMap wvmSim; IVectorComparison vSim;
            if (pr == PageRank.SIM01EBE) {
                wvmSim = WordVectorMapFactory.getESA01Vectors();                 
                vSim = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);
            }
            else if (pr == PageRank.SIMCOS) {
                wvmSim = WordVectorMapFactory.getESAVectors();                 
                vSim = new VectorSimilarity(SimilarityMeasure.COSINE);                
            }
            else throw new UnsupportedOperationException();             
            TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlpAll();
            dvec = new TermPageRankVectorizer(compWvm, wvmSim, vSim, cform, 
                    tdf, aggMeth, prDf, prSm);                    
        }
        else if (doc == DocAgg.TF_SUM) {
            dvec = new TermFrequencyVectorizer(compWvm, cform);
        }
        else throw new UnsupportedOperationException(); 
        
        // phrase set vectorization
        IPhraseSetVectorizer phvec;
        if (ph == PhAgg.UW_SUM) {
            phvec = new SumPhraseSetVectorizer(compWvm);
        }
        else if (ph == PhAgg.UW_MAX) {
            phvec = new MaxPhraseSetVectorizer(compWvm);
        }
        else if (ph == PhAgg.UW_WEIGHTED) {
            IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfSemevalStemOpenNlpAll(), 0);
            phvec = new WSumPhraseSetVectorizer(compWvm, scr);
        }
        else if (ph == PhAgg.PH_SUM) {
            phvec = new PhSumPhraseSetVectorizer(compWvm, null);
        }
        else if (ph == PhAgg.PH_SUM_WEIGHTED) {
            IPhraseScore scr = new RankerExtractor(null, DfFactory.loadDfSemevalStemOpenNlpAll(), 0);            
            phvec = new PhSumPhraseSetVectorizer(compWvm, scr);
        }
        else throw new UnsupportedOperationException();
        
        // vector comparison
        IVectorComparison cmp;        
        if (vecq == VecQ.COS) {
           cmp = new VectorSimilarity(SimilarityMeasure.COSINE);  
        }
        else if (vecq == VecQ.COS_CUT) {
            cmp = new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF);  
        }
        else if (vecq == VecQ.EBE) {
            cmp = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);  
        }
        else throw new UnsupportedOperationException();         
                   
        return new GreedyExtractorConfig(wvm, vecMod, cform, dvec, phext, phvec, cmp);        
    }
    
    public static GreedyExtractorConfig getLSICosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.LEMMA;
        IWordToVectorMap wvm = WordVectorMapFactory.getLSIVectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.NONE, cform, dvec, phext, phvec, cmp);
    }
    
    public static GreedyExtractorConfig getESATfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.NONE, cform,
                dvec, phext, phvec, cmp);
    }    
    
    public static GreedyExtractorConfig getESA01TfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.NONE, cform, dvec, phext, phvec, cmp);
    }       
    
    public static GreedyExtractorConfig getESA01TfEbeExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);     
        return new GreedyExtractorConfig(wvm, VectorMod.NONE, cform, dvec, phext, phvec, cmp);
    }         
    
    public static GreedyExtractorConfig getESA01TfIdfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        // wort to vector mapping
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        // document vetorization
        TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlpAll();
        IDocumentVectorizer dvec = new TfIdfVectorizer(wvm, cform, tdf, Method.SUM);
        // phrase extractor
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));   
        // phrase set vectorization
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        // vector comparison
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.NONE, cform, dvec, phext, phvec, cmp);
    }    
    
    public static GreedyExtractorConfig getESAPrunedTfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(null, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(null);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.PRUNE, cform , 
                                            dvec, phext, phvec, cmp);
    }       

    public static GreedyExtractorConfig getESA01PrunedTfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(null, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(null);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(wvm, VectorMod.PRUNE, cform , 
                                            dvec, phext, phvec, cmp);
    }     
    
}
