package hr.irb.zel.kpelab.coverage.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.util.MapCache;

public class MaxWordSimilarityCoverage implements IPhraseCoverage, IPhraseSimilarityCalculator {

    IWordSimilarityCalculator wordSim;
    boolean average;
    MapCache<String, Double> cache;
    
    private static final int cacheCapacity = 50000;
    
    public MaxWordSimilarityCoverage(IWordSimilarityCalculator wsim, boolean avg) {
        wordSim = wsim; average = avg;
        cache = new MapCache<String, Double>(cacheCapacity);
    }

    /** Get coverage from cache or calculate it. */
    public double coverage(Phrase ph1, Phrase ph2)  throws SimilarityCalculationException {    
        String key = getPhrasePairKey(ph1, ph2);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        else {
            double val = calculateCoverage(ph1, ph2);
            cache.put(key, val);
            return val;
        }
    }
    
    // get string representation of the pair
    private String getPhrasePairKey(Phrase ph1, Phrase ph2) {
        return ph1.canonicForm() + "|" + ph2.canonicForm();
    }
    
    /** For each token of ph2, calculate max. similarity with tokens form ph1, 
     * return the sum of these max. similarities for each token in ph2. */
    public double calculateCoverage(Phrase ph1, Phrase ph2)  throws SimilarityCalculationException {
        double res = 0;
        for (String w2 : ph2.getCanonicTokens()) {
            double maxSim = Double.MIN_VALUE;
            for (String w1 : ph1.getCanonicTokens()) {
                double sim = wordSim.similarity(w1,w2);
                if (sim > maxSim) maxSim = sim;
            }
            res += maxSim;
        }
        if (average) res /= ph2.getCanonicTokens().size();
        return res;
    }

    /** This adapter method is implemented so that the class can be tested as a similarity measure. */
    public double similarity(Phrase ph1, Phrase ph2) throws SimilarityCalculationException {
        return coverage(ph1, ph2);
    }

}
