package hr.irb.zel.kpelab.evaluation;

import hr.irb.zel.kpelab.phrase.Phrase;

/** Testing equality of two phrases. */
public interface IPhraseEquality {

    public enum PhEquality { CANONIC, SEMEVAL }
    
    /** Operation is in general asymmetric, second phrase is assumed
     * to belong to a set of correct phrases read from a dataset. */
    public boolean equal(Phrase solution, Phrase correct);
    
}
