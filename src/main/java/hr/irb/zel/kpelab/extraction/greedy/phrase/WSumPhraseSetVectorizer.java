/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction.greedy.phrase;

import hr.irb.zel.kpelab.phrase.IPhraseScore;
import hr.irb.zel.kpelab.phrase.Phrase;
import vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/** Vectorizes the phrase set as sum of vectors of cannonic word 
 representations (stems or lemmas) weighted with a phrase score. Each word is counted once. */
public class WSumPhraseSetVectorizer implements IPhraseSetVectorizer {

    private IWordToVectorMap wordToVector;
        
    private IRealVector vector; // current vector    
    private Map<String, Integer> words; // word of the phrase set and their counts
    private Phrase lastAdded;    
    private IPhraseScore score;
    
    public WSumPhraseSetVectorizer(IWordToVectorMap wvm, IPhraseScore scr) { 
        wordToVector = wvm; 
        words = new TreeMap<String, Integer>();
        vector = null;       
        lastAdded = null;     
        score = scr;
    }
    
     public String getId() { return "uwordwsum"; }
    
    public void setVectors(IWordToVectorMap wvmap) { 
        wordToVector = wvmap; 
        clear();
    }    
    
    public void addPhrase(Phrase ph) throws Exception {
        boolean newWord = false;        
//        System.out.println("* adding phrase: " + ph);
//        System.out.println("wordsBefore");
//        printWords();
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
        double s = score.score(ph);
        if (newWord) {
            for (String w : diffWords) {
                IRealVector v = wordToVector.getWordVector(w);                
                if (v == null) continue;
                v = v.clone(); v.multiply(s);
                if (vector == null) vector = v; // init vector as a copy of v                                
                else vector.add(v);                
            }
        }      
//        System.out.println("wordsAfter");
//        printWords();        
    }

    // for debug
    private void printWords() {
        System.out.print("-words: ");
        for (String w : words.keySet()) System.out.print(w+" ");
        System.out.println();
    }
    
    public void removeLastAdded() throws Exception {
        if (lastAdded == null) return;
        List<String> diffWords = new ArrayList<String>(10);  
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
        if (removed) {
            double s = score.score(lastAdded);
            for (String w : diffWords) {
                IRealVector v = wordToVector.getWordVector(w);
                if (v == null) continue; 
                v = v.clone(); v.multiply(s);
                vector.subtract(v);
            }
        }
        lastAdded = null;
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
