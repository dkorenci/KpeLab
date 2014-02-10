package hr.irb.zel.kpelab.similarity.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;

/** Symmetric measure that expresses the degree of similarity between two phrases. */ 
public interface IPhraseSimilarityCalculator {

    public double similarity(Phrase ph1, Phrase ph2) throws SimilarityCalculationException;    
    
}
