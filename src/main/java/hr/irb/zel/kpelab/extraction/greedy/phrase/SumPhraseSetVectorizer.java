/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction.greedy.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Vectorizes the phrase set as sum of vectors of cannonic word 
 representations (stems or lemmas). Each word is counted once. */
public class SumPhraseSetVectorizer implements IPhraseSetVectorizer {

    private IWordToVectorMap wordToVector;
        
    private IRealVector vector; // current vector    
    private Map<String, Integer> words; // word of the phrase set and their counts
    private Map<String, Integer> wordsBeforeAdd; // words before adding last added phrase
    private Phrase lastAdded;    
    private IRealVector vectorBeforeAdd; // vector before adding last added phrase
    boolean addition; // true if there was an addPhrase() call and no subsequent removeLastAdded()
    
    public SumPhraseSetVectorizer(IWordToVectorMap wvm) { 
        wordToVector = wvm; 
        words = new HashMap<String, Integer>();     
    }
    
    public void addPhrase(Phrase ph) throws Exception {        
        addition = true;
        // save state before add (clone vector and words)       
        if (vector != null) vectorBeforeAdd = vector.clone();
        else vectorBeforeAdd = null;
        wordsBeforeAdd = new HashMap<String, Integer>(words);
        // update words map
        boolean newWord = false;      
        List<String> diffWords = new ArrayList<String>(10);  
        for (String w : ph.getCanonicTokens()) {
            if (words.containsKey(w)) words.put(w, words.get(w)+1);
            else { 
                words.put(w, 1);
                newWord = true;
                diffWords.add(w);
            }
        }         
        lastAdded = ph;
        // add newly added words to vectors        
        if (newWord) {
            for (String w : diffWords) {
                //System.out.print(w+";");
                IRealVector v = wordToVector.getWordVector(w);
                if (v == null) continue;
                if (vector == null) vector = v.clone(); // init vector as a copy of v                                
                else vector.add(v);                
            }
            //System.out.println();
        }        
    }

    public void removeLastAdded() throws Exception {
        if (addition) {
            vector = vectorBeforeAdd;
            vectorBeforeAdd = null;
            words = wordsBeforeAdd;
            wordsBeforeAdd = null;
            addition = false;
        }
//        if (lastAdded == null) return;
//        List<String> diffWords = new ArrayList<String>(10);  
//        boolean removed = false;
//        for (String w : lastAdded.getCanonicTokens()) {
//            assert(words.containsKey(w));
//            if (words.get(w) > 1) words.put(w, words.get(w)-1);
//            else { 
//                words.remove(w);
//                removed = true;
//                diffWords.add(w);
//            }
//        }        
//        lastAdded = null;
//        if (removed) {
//            for (String w : diffWords) {
//                IRealVector v = wordToVector.getWordVector(w);
//                if (v == null) continue;       
//                vector.subtract(v);
//            }
//        }
    }

    public IRealVector vector() {
        return vector;
    }

    public void clear() {        
        words.clear();
        vector = null;
        lastAdded = null;
    }        

    public boolean isNull(Phrase ph) {
        for (String w : ph.getCanonicTokens()) {
            if (wordToVector.hasWord(w)) return false;
        }
        return true;
    }

}
