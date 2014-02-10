package hr.irb.zel.kpelab.coverage.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;

/**
 * Metric that expresses how much is phrase ph2 semantically covered by phrase ph1.
 * Not necessarily symmetric.
 */
public interface IPhraseCoverage {
    
    public double coverage(Phrase ph1, Phrase ph2) throws Exception;
    
}
