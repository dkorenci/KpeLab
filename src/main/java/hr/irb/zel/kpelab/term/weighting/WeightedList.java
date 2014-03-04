/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.term.weighting;

import hr.irb.zel.kpelab.term.WeightedTerm;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Create ITermWeight that returns weight explicitly assigned via 
 * List<WeightedTerm> termFreqs.
 */
public class WeightedList implements ITermWeight {

    private Map<String, Double> weights;        
    
    public WeightedList(List<WeightedTerm> termFreqs) {
        weights = new TreeMap<String, Double>();
        for (WeightedTerm wt : termFreqs) weights.put(wt.term, wt.weight);        
    }

    public double weight(String term) {        
        if(weights.containsKey(term)) return weights.get(term);
        else throw new IllegalArgumentException(
                "term: " + term + " has not been assigned weight");
    }
}
