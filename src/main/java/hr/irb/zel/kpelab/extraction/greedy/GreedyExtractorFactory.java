package hr.irb.zel.kpelab.extraction.greedy;

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
        IVectorComparison cmp = new VectorSimilarity(SimilarityMeasure.COSINE);     
        return new GreedyExtractorConfig(dvec, phext, phvec, cmp);
    }
    
}
