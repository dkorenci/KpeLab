package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.df.DfFactory;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.extraction.greedy.phrase.SumPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity.SimilarityMeasure;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import hr.irb.zel.kpelab.vectors.document.TermFrequencyVectorizer;
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
        TFIDF_SUM // sum content words mult. by tfidf
    }
    // phrase set to vector aggregation method
    public enum PhAgg { 
        UW_SUM // sum of vectors of unique words
    }
    // quality measure that compares phrase set and document vectors
    public enum VecQ { COS, COS_CUT, EBE }
    
    // creates extractor config based on options
    public static GreedyExtractorConfig create(Vec vec, boolean vec01, boolean vecPr, 
            DocAgg doc, PhAgg ph, VecQ vecq) throws Exception {
        // canonic form
        CanonicForm cform;
        if (vec == Vec.LSI) cform = CanonicForm.LEMMA;
        else if (vec == Vec.ESA) cform  = CanonicForm.STEM;
        else throw new UnsupportedOperationException(); 
        
        // phrase extractor
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));    
        
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
        if (vecPr) compWvm = null; else compWvm = wvm;        
        
        // document vectorization
        IDocumentVectorizer dvec;
        if (doc == DocAgg.TFIDF_SUM) {   
            TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlp();
            dvec = new TfIdfVectorizer(compWvm, cform, tdf);
        }
        else if (doc == DocAgg.TF_SUM) {
            dvec = new TermFrequencyVectorizer(compWvm, cform);
        }
        else throw new UnsupportedOperationException(); 
        
        // phrase set vectorization
        IPhraseSetVectorizer phvec;
        if (ph == PhAgg.UW_SUM) {
            phvec = new SumPhraseSetVectorizer(wvm);
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
        
        // vector modification flag
        VectorMod vmod;
        if (vecPr == false) vmod = VectorMod.NONE;
        else vmod = VectorMod.PRUNE_UNIQUE;
                    
        return new GreedyExtractorConfig(wvm, vmod, cform, dvec, phext, phvec, cmp);        
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
        TermDocumentFrequency tdf = DfFactory.loadDfSemevalStemOpenNlp();
        IDocumentVectorizer dvec = new TfIdfVectorizer(wvm, cform, tdf);
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
        return new GreedyExtractorConfig(wvm, VectorMod.PRUNE_UNIQUE, cform , 
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
        return new GreedyExtractorConfig(wvm, VectorMod.PRUNE_UNIQUE, cform , 
                                            dvec, phext, phvec, cmp);
    }     
    
}
