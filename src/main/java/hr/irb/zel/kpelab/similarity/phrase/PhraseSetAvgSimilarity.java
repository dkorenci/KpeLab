package hr.irb.zel.kpelab.similarity.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import java.util.List;

public class PhraseSetAvgSimilarity implements IPhraseSetSimilarity {

    IPhraseSimilarityCalculator phraseSim;
    
    public PhraseSetAvgSimilarity(IPhraseSimilarityCalculator phsim) {
        phraseSim = phsim;
    }
    
    public double similarity(List<Phrase> phraseSet) throws SimilarityCalculationException {
        if (phraseSet.isEmpty()) return 0;
        if (phraseSet.size() == 1) return 1;
        
        int numPairs = 0; double sim = 0;        
        for (int i = 0; i < phraseSet.size(); ++i) {
            for (int j = i + 1; j < phraseSet.size(); ++j) {
                Phrase ph1 = phraseSet.get(i), ph2 = phraseSet.get(j);
                sim += phraseSim.similarity(ph1, ph2);
                numPairs++;
            }
        }
        
        return sim/numPairs;
    }

}
