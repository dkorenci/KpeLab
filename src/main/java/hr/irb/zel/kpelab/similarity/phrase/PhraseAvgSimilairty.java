package hr.irb.zel.kpelab.similarity.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import java.util.List;

/**
 * Calculates phrase similarity by averaging similarities of words.
 */
public class PhraseAvgSimilairty implements IPhraseSimilarityCalculator {

    IWordSimilarityCalculator wordSim;
    
    public PhraseAvgSimilairty(IWordSimilarityCalculator wsim) { wordSim = wsim; }

    public double similarity(Phrase ph1, Phrase ph2) throws SimilarityCalculationException {
        List<String> words1 = ph1.getCanonicTokens();
        List<String> words2 = ph2.getCanonicTokens();
        
        double sim = 0.;
        for (String w1 : words1) {
            for (String w2 : words2) sim += wordSim.similarity(w1,w2);
        }

        sim /= words1.size() + words2.size();
        
        return sim;
    }
    
}
