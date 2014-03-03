/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction.greedy.phrase;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TIntDoubleProcedure;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.VectorEntry;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Vectorizes the phrase set as coordinate-wise max. of vectors of cannonic word 
 representations (stems or lemmas). Each word is counted once. */
public class MaxPhraseSetVectorizer implements IPhraseSetVectorizer {

    private IWordToVectorMap wordToVector;
        
    private IRealVector vector; // current vector    
    private Map<String, Integer> words; // word of the phrase set and their counts    
    TIntDoubleMap oldValues; // old values of coordinates that changed with max.
    boolean vectorWasNull; // indicates that (current) vector was initialized from null in previous add
    private Phrase lastAdded;  
    
    public MaxPhraseSetVectorizer(IWordToVectorMap wvm) { 
        wordToVector = wvm; 
        words = new TreeMap<String, Integer>();
        clear();
    }
    
    public void clear() {        
        words.clear();
        vector = null;
        lastAdded = null;
    }    
    
    public String getId() { return "uwordmax"; }
    
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
        
        oldValues = new TIntDoubleHashMap();
        vectorWasNull = false;
        lastAdded = ph;
        
        if (newWord) {
            for (String w : diffWords) {
                IRealVector v = wordToVector.getWordVector(w);
                if (v == null) continue;
                if (vector == null) {
                    vector = v.clone();
                    vectorWasNull = true;
                } // init vector as a copy of v                                
                else {
                    for (VectorEntry e : v.getNonZeroEntries()) {                        
                        int c = e.coordinate; double val = e.value;
                        Double oldVal = vector.element(c);
                        if (vectorWasNull) {
                            // vector will be set to null at removeLastAdded
                            // so no storing of old values
                            if (oldVal < val) vector.setElement(c, val);
                        }
                        else {
                            // if oldValue will be overwritten, for the first time 
                            // (it is the value from original vector), store it
                            if (oldVal < val && oldValues.containsKey(c) == false) {
                                oldValues.put(e.coordinate, oldVal);
                                vector.setElement(c, val);
                            }
                        }
                    }                    
                }                
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
        boolean removed = false;
        for (String w : lastAdded.getCanonicTokens()) {
            assert(words.containsKey(w));
            if (words.get(w) > 1) words.put(w, words.get(w)-1);
            else { 
                words.remove(w);
                removed = true;                
            }
        }        
        lastAdded = null;
        if (removed) {
            if (vectorWasNull) vector = null;
            else {
                oldValues.forEachEntry(new TIntDoubleProcedure() {
                    public boolean execute(int i, double d) {
                        vector.setElement(i, d);
                        return true;
                    }
                });                
            }
        }
    }

    public IRealVector vector() {
        return vector;
    }   

    public boolean isNull(Phrase ph) {
        for (String w : ph.getCanonicTokens()) {
            if (wordToVector.hasWord(w)) return false;
        }
        return true;
    }


}
