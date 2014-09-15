/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.coverage.phrase.IPhraseCoverage;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class RandomPhraseExtractor implements IKpextractor {

    PosRegexPhraseExtractor extractor;    
    private int phraseSetSize;     
    Random randomGen;
    List<Phrase> phrases;       
    
    public RandomPhraseExtractor(PosRegexPhraseExtractor extr, int K) {
        extractor = extr; phraseSetSize = K;
    }
    
    public List<Phrase> extract(String text) throws Exception {
        phrases = extractor.extractPhrases(text);
        Set<Phrase> uniquePhrases = new TreeSet<Phrase>(phrases);        
        phrases = new ArrayList<Phrase>(uniquePhrases);        
        randomGen = new Random(7786654);
        
        if (phrases.size() <= phraseSetSize) return phrases;
        else {
            return getRandomPhraseSet(phraseSetSize);
        }             
    }    

    public String getId() { return "random"; }
    
    private List<Phrase> getRandomPhraseSet(int size) {
        List<Phrase> ph = new ArrayList<Phrase>();
        List<Phrase> allPhrases = new ArrayList<Phrase>(phrases);        
        for (int i = 0; i < size && i < phrases.size(); ++i) {
            int rndIndex = randomGen.nextInt(allPhrases.size());
            ph.add(allPhrases.get(rndIndex));
            allPhrases.remove(rndIndex);
        }
        return ph;
    }    

}
