package hr.irb.zel.kpelab.term.weighting;

import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.term.WeightedTerm;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class TfIdfTermWeight implements ITermWeight {

    private Map<String, Double> tfMap;
    private TermDocumentFrequency dfMap;
    private int N;
    
    public TfIdfTermWeight(List<WeightedTerm> termFreqs, TermDocumentFrequency df) {
        tfMap = new TreeMap<String, Double>();
        for (WeightedTerm wt : termFreqs) tfMap.put(wt.term, wt.weight);        
        dfMap = df;
        N = dfMap.getNumDocuments();
    }

    public double weight(String term) {
        double tf, idf;
        if(tfMap.containsKey(term)) tf = tfMap.get(term);
        else tf = 1;
        idf = dfMap.documentFrequency(term);
        if (idf == 0) idf = 1;
        tf = 1 + Math.log(tf);
        idf = Math.log(N/idf);
        return tf * idf;
    }
    
}
