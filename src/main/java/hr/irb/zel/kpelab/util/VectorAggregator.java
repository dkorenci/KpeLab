package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.term.WeightedTerm;
import hr.irb.zel.kpelab.term.weighting.ITermWeight;
import vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.Arrays;
import java.util.Collection;

public class VectorAggregator {
    
    IWordToVectorMap wordToVector;
    
    public static enum Method { MAX, SUM } // aggregation method
    
    public VectorAggregator(IWordToVectorMap wvmap) { wordToVector = wvmap; }
        
    /** Aggregate vectors of tokens that are present in the map, 
     * use specified aggregation method. Return null if no token is in the map. */
    public IRealVector aggregate(Collection<String> tokens, Method method) throws Exception {
        IRealVector vector = null;
        for (String tok : tokens) {
            if (wordToVector.hasWord(tok)) {
                if (vector == null) 
                    vector = wordToVector.getWordVector(tok).clone();
                else {
                    if (method == Method.MAX)
                        vector.maxMerge(wordToVector.getWordVector(tok));    
                    else if (method == Method.SUM)
                        vector.add(wordToVector.getWordVector(tok));
                }
            }
        }        
        return vector;        
    }

    public IRealVector aggregateWeighted(Collection<String> tokens,
            ITermWeight tweight, Method method) throws Exception {
        IRealVector vector = null;
        for (String tok : tokens) {
            if (wordToVector.hasWord(tok)) {
                double w = tweight.weight(tok);
                if (vector == null) {
                    vector = wordToVector.getWordVector(tok).clone();
                    vector.multiply(w);
                }
                else {
                    IRealVector v = wordToVector.getWordVector(tok).clone();
                    if (method == Method.MAX) vector.maxMerge(v.multiply(w));    
                    else if (method == Method.SUM) vector.add(v.multiply(w));
                    else throw new UnsupportedOperationException("unsupported aggregation method");
                }
            }
        }        
        return vector;        
    }    
    
    /** Return sum of term vectors multiplied by log term frequencies. */
    public IRealVector sumWeighted(Collection<WeightedTerm> terms) throws Exception {
        IRealVector vector = null;
        for (WeightedTerm tw : terms) {
            if (wordToVector.hasWord(tw.term)) {
                if (vector == null) {
                    vector = wordToVector.getWordVector(tw.term).clone();
                    double factor = 1 + Math.log(tw.weight);
                    vector.multiply(factor);
                }
                else {
                    IRealVector v = wordToVector.getWordVector(tw.term).clone();
                    double factor = 1 + Math.log(tw.weight);                    
                    vector.add(v.multiply(factor));
                }
            }
        }        
        return vector;            
    }
    
    /** Tokenize string by whitespace and aggregate. */
    public IRealVector aggregate(String str, Method method) throws Exception {
        return aggregate(Arrays.asList(str.split("\\s")), method);
    }
    
}
