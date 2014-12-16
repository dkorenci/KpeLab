package hr.irb.zel.kpelab.similarity.word;

import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import vectors.comparison.IVectorComparison;

/** Calculates word similarity based on similarity of vector representation of words. */
public class VectorWordSimilarity implements IWordSimilarityCalculator {
    
    private IWordToVectorMap wordVecMap;
    private IVectorComparison vectorSim;
    
    public VectorWordSimilarity(IWordToVectorMap wvm, IVectorComparison vsim) {
        wordVecMap = wvm; vectorSim = vsim;
    } 
    
    public double similarity(String w1, String w2) throws SimilarityCalculationException {
        IRealVector v1, v2;
        // get vectors from map
        try {
            v1 = wordVecMap.getWordVector(w1);
            v2 = wordVecMap.getWordVector(w2);
        }
        catch(Exception e) {
            throw new SimilarityCalculationException(e);
        }
        // check if the words are in the map
        if (v1 == null || v2 == null) {
            String message = "";
            if (v1 == null) message = "word: " + w1 + " not in a map ";
            if (v2 == null) message += "word: " + w2 + " not in a map";
            throw new SimilarityCalculationException(message);
        }
        // calculate distance
        return vectorSim.compare(v1, v2);
    }

}
