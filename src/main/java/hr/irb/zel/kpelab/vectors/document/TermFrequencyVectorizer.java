package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.term.WeightedTerm;
import hr.irb.zel.kpelab.util.VectorAggregator;
import vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Sum vectors of adjectives and nouns weighted with function of their frequency.
 */
public class TermFrequencyVectorizer implements IDocumentVectorizer {

    private IWordToVectorMap wordToVector;
    private CanonicForm cform; // canonic form of extracted words
    private TermExtractor termExtr;
    private VectorAggregator agg;
    
    public void setVectors(IWordToVectorMap wvmap) { 
        wordToVector = wvmap; 
    }    
    
    public String getId() { return "tfsum"; }
    
    public TermFrequencyVectorizer(IWordToVectorMap wvm, CanonicForm cf) throws UIMAException
    { 
        wordToVector = wvm; cform = cf;
        termExtr = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, cform));        
    }
    
    public IRealVector vectorize(String txt) throws Exception {
        agg = new VectorAggregator(wordToVector);
        List<WeightedTerm> wterms = termExtr.extractWeighted(txt);        
        return agg.sumWeighted(wterms);
    }

}
