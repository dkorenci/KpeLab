package hr.irb.zel.kpelab.extraction.tabu;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;

/**
 * Interface for manipulating a set of phrases from 
 * a local search based optimization algorithm.
 */
public interface ISearchPhraseSet {
    
    public void setDocument(KpeDocument doc) throws Exception;
    public void setPhraseSet(List<Phrase> phraseSet) throws Exception;
    //public void addPhrase(Phrase ph);
    //public void removePhrase(int i);
    public void replacePhrase(int i, Phrase ph) throws Exception;
    //public boolean contains(Phrase ph);
    public List<Phrase> getDocumentPhrases();    
    public List<Phrase> getPhrases();
    
    public Phrase getPhrase(int i);
    public int numPhrases();
    public boolean containsPhrase(Phrase ph);
    
    public double calculateQuality();    
    
    public void printDebugData();
}
