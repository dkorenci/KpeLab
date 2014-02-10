package hr.irb.zel.kpelab.similarity.word;

import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;

public interface IWordSimilarityCalculator {
   
    public double similarity(String w1, String w2) throws SimilarityCalculationException;
    
}
