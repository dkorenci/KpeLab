package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.term.WeightedTerm;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Sum vectors of text words weighted with their (modified) frequency.
 */
public class TermFrequencyVectorizer implements IDocumentVectorizer {

    private IWordToVectorMap wordToVector;
    private CanonicForm cform; // canonic form of extracted words
    private TermExtractor termExtr;
    private VectorAggregator agg;
    
    public TermFrequencyVectorizer(IWordToVectorMap wvm, CanonicForm cf) throws UIMAException
    { 
        wordToVector = wvm; cform = cf;
        termExtr = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, cform));        
        agg = new VectorAggregator(wordToVector);
    }
    
    public IRealVector vectorize(String txt) throws Exception {
        List<WeightedTerm> wterms = termExtr.extractWeighted(txt);        
        return agg.sumWeighted(wterms);
    }

}
