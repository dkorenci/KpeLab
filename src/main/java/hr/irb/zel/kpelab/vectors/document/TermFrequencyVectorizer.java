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
    private String text;
    private CanonicForm cform; // canonic form of extracted words
    
    public TermFrequencyVectorizer(IWordToVectorMap wvm, CanonicForm cf)
    { wordToVector = wvm; cform = cf; }
    
    public IRealVector vectorize(String txt) throws Exception {
        text = txt;
        VectorAggregator agg = new VectorAggregator(wordToVector);
        TermExtractor textr = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, cform));
        List<WeightedTerm> wterms = textr.extractWeighted(txt);        
        return agg.sumWeighted(wterms);
    }

}
