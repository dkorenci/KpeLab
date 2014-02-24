/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction.greedy.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Vectorizes the phrase set as sum of vectors of cannonic word 
 representations (stems or lemmas). Each word is counted once. */
public class SumPhraseSetVectorizer implements IPhraseSetVectorizer {

    private IWordToVectorMap wordToVector;
        
    private IRealVector vector; // current vector    
    private Map<String, Integer> words; // word of the phrase set and their counts
    private Phrase lastAdded;
    private List<String> diffWords; // words that were added or removed by last operation
    
    public SumPhraseSetVectorizer(IWordToVectorMap wvm) { 
        wordToVector = wvm; 
        words = new HashMap<String, Integer>();
        vector = null;       
        lastAdded = null;
        diffWords = new ArrayList<String>(10);       
    }
    
    public void addPhrase(Phrase ph) throws Exception {
        boolean newWord = false;
        diffWords.clear();
        for (String w : ph.getCanonicTokens()) {
            if (words.containsKey(w)) words.put(w, words.get(w)+1);
            else { 
                words.put(w, 1);
                newWord = true;
                diffWords.add(w);
            }
        }
        lastAdded = ph;
        if (newWord) {
            for (String w : diffWords) {
                IRealVector v = wordToVector.getWordVector(w);
                if (v == null) continue;
                if (vector == null) vector = v.clone(); // init vector as a copy of v                                
                else vector.add(v);                
            }
        }        
    }

    public void removeLastAdded() throws Exception {
        if (lastAdded == null) return;
        diffWords.clear();
        boolean removed = false;
        for (String w : lastAdded.getCanonicTokens()) {
            assert(words.containsKey(w));
            if (words.get(w) > 1) words.put(w, words.get(w)-1);
            else { 
                words.remove(w);
                removed = true;
                diffWords.add(w);
            }
        }        
        lastAdded = null;
        if (removed) {
            for (String w : diffWords) {
                IRealVector v = wordToVector.getWordVector(w);
                if (v == null) continue;       
                vector.subtract(v);
            }
        }
    }

    public IRealVector vector() {
        return vector;
    }
    
    

}
