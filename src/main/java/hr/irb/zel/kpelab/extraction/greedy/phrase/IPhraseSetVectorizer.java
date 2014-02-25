package hr.irb.zel.kpelab.extraction.greedy.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;

/**  Interface for manipulating a phrase set from a greedy algorithm.  */
public interface IPhraseSetVectorizer {

    // add phrase to the set
    public void addPhrase(Phrase ph) throws Exception; 
    // remove most recently added phrase
    public void removeLastAdded() throws Exception;
    // get vector representation of the phrase set
    public IRealVector vector() throws Exception;
    // remove all phrases from the set
    public void clear();    
    // true if there is no mapping to vector for any word in the phrase
    public boolean isNull(Phrase ph);
    
}
