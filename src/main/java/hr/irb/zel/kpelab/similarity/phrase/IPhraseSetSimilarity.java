package hr.irb.zel.kpelab.similarity.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import java.util.List;

public interface IPhraseSetSimilarity {
    public double similarity(List<Phrase> phraseSet) throws SimilarityCalculationException;     
}
