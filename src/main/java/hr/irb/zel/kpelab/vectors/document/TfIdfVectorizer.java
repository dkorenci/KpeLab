package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.term.WeightedTerm;
import hr.irb.zel.kpelab.term.weighting.TfIdfTermWeight;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.util.VectorAggregator.Method;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Sum vectors of adjectives and nouns weighted with function of their frequency.
 */
public class TfIdfVectorizer implements IDocumentVectorizer {

    private IWordToVectorMap wordToVector;
    private CanonicForm cform; // canonic form of extracted words
    private TermExtractor termExtr;
    private VectorAggregator agg;
    private TermDocumentFrequency tdf;
    
    public TfIdfVectorizer(IWordToVectorMap wvm, CanonicForm cf, 
            TermDocumentFrequency df) throws UIMAException
    { 
        wordToVector = wvm; cform = cf;
        termExtr = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, cform));        
        agg = new VectorAggregator(wordToVector);
        tdf = df;
    }
    
     public String getId() { return "tfidfsum"; }
    
    public void setVectors(IWordToVectorMap wvmap) { 
        wordToVector = wvmap; 
        agg = new VectorAggregator(wordToVector);
    }        
    
    public IRealVector vectorize(String txt) throws Exception {
        List<WeightedTerm> wterms = termExtr.extractWeighted(txt); 
        List<String> terms = new ArrayList<String>(wterms.size());
        for (WeightedTerm wt : wterms) { terms.add(wt.term); }                   
        TfIdfTermWeight tfidf = new TfIdfTermWeight(wterms, tdf);
        return agg.aggregateWeighted(terms, tfidf, Method.SUM);
    }

}
