package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.df.DfFactory;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
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

/** Methods for creating various GreedExtractor configurations. */
public class GreedyExtractorFactory {

//    public GreedyExtractorConfig(IDocumentVectorizer dvec, IPhraseExtractor phext, 
//            IPhraseSetVectorizer phvec, IVectorComparison cmp) {    
    
    public static GreedyExtractorConfig getLSICosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.LEMMA;
        IWordToVectorMap wvm = WordVectorMapFactory.getLSIVectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF);     
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
    }
    
    public static GreedyExtractorConfig getESATfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESAVectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
    }    
    
    public static GreedyExtractorConfig getESA01TfCosExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
    }       
    
    public static GreedyExtractorConfig getESA01TfEbeExtractor() throws Exception {
        CanonicForm cform = CanonicForm.STEM;
        IWordToVectorMap wvm = WordVectorMapFactory.getESA01Vectors();
        IDocumentVectorizer dvec = new TermFrequencyVectorizer(wvm, cform);
        IPhraseExtractor phext = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, cform));           
        IPhraseSetVectorizer phvec = new SumPhraseSetVectorizer(wvm);
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.EBE_MULTIPLY);     
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
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
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
    }    
    
    
    
}
