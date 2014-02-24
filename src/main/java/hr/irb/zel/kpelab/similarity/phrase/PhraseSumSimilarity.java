package hr.irb.zel.kpelab.similarity.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import java.util.List;

/**
 * Calculates phrase similarity by summing word vectors for each phrase 
 * and returning distance between phrase vectors.
 */
public class PhraseSumSimilarity implements IPhraseSimilarityCalculator {

    IWordToVectorMap wordToVector;
    IVectorComparison vectorSim;
    
    public PhraseSumSimilarity(IWordToVectorMap wvm, IVectorComparison vsim) {
        wordToVector = wvm; vectorSim = vsim;
    }
    
    public double similarity(Phrase ph1, Phrase ph2) throws SimilarityCalculationException {
        List<String> words1 = ph1.getCanonicTokens();
        List<String> words2 = ph2.getCanonicTokens();
        
        IRealVector v1, v2;
        try {
            v1 = sumWordVectors(words1);
            v2 = sumWordVectors(words2);
            return vectorSim.compare(v1, v2);
        }
        catch (Exception e) {
            throw new SimilarityCalculationException(e);
        }                
    }        

    private IRealVector sumWordVectors(List<String> words) throws Exception {
        IRealVector result = wordToVector.getWordVector(words.get(0)).clone();
        for (int i = 1; i < words.size(); ++i) {
            result.add(wordToVector.getWordVector(words.get(i)));
        }        
        return result;
    }
    
}
