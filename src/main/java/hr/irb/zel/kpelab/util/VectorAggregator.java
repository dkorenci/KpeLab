package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.vectors.IRealVector;
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
    
    /** Tokenize string by whitespace and aggregate. */
    public IRealVector aggregate(String str, Method method) throws Exception {
        return aggregate(Arrays.asList(str.split("\\s")), method);
    }
    
}