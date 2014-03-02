package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.ArrayRealVector;
import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Cache for word to vector mapping, adds (word, vector) pairs until
 * maximum capacity for number of words is reached, after that
 * words that is least recently added is removed from cache before adding new word.
 */
public class WordToVectorMapCache implements Serializable, IWordToVectorMap {
    
    private static final long serialVersionUID = 8194056635287140192L;

    private Map<String, Integer> wordToIndex; // word to index of vector map
    private List<IRealVector> vectors; // vectors of words in cache    
    private List<String> words; // list of all words in cache
    
    private int mostRecentWord; // index of the most recenly added word 
    private int capacity;
    
    // null cannot be serialized so alternative representation of null vector is needed
    private IRealVector nullVector; 
    
    public WordToVectorMapCache(int cap) { 
        capacity = cap; 
        mostRecentWord = -1;
        wordToIndex = new TreeMap<String, Integer>();
        vectors = new ArrayList<IRealVector>();
        words = new ArrayList<String>();        
        nullVector = new ArrayRealVector(13, 0.0);
    }
    
    public IRealVector getWordVector(String word) {
        if (wordToIndex.containsKey(word)) {
            IRealVector v = vectors.get(wordToIndex.get(word));
            if (v.equals(nullVector)) return null;
            else return v;
        }
        else return null;
    }

    public boolean hasWord(String word) {
        return wordToIndex.containsKey(word);
    }    
    
    /** Add mapping word->vector. */
    public void addWordVectorPair(String word, IRealVector vector) {                
        int vectorIndex;        
        if (vector == null) vector = nullVector;        
        if (hasWord(word)) { // word is in the map, replace vector
            vectorIndex = wordToIndex.get(word);
            vectors.set(vectorIndex, vector);
            return;
        }        
        if (size() == capacity) {            
            // get last word, ie word after most recently added 
            mostRecentWord++; 
            if (mostRecentWord == capacity) mostRecentWord = 0;   
            // remove last word
            String removed = words.get(mostRecentWord);
            vectorIndex = wordToIndex.get(removed);
            wordToIndex.remove(removed);
            // add new word in place of last word
            words.set(mostRecentWord, word);
            vectors.set(vectorIndex, vector);
            wordToIndex.put(word, vectorIndex);
        }        
        else {            
            mostRecentWord++; 
            words.add(word);
            vectors.add(vector);
            wordToIndex.put(word, mostRecentWord);
        }
    }
    
    public void printCache() {
        System.out.println("-------------------");
        System.out.println("words: (number="+words.size()+")");
        for (int i = 0; i < words.size(); ++i) 
            System.out.println(words.get(i));
        System.out.println("vectors: (number="+vectors.size()+")");
        for (int i = 0; i < vectors.size(); ++i) 
            System.out.println(vectors.get(i));
        System.out.println("map: (size="+ wordToIndex.size() +")");
        for (String w : wordToIndex.keySet()) {
            System.out.println("w: " + w + " , index: " + wordToIndex.get(w));
        }
        System.out.println("-------------------");
    }
    
    private int size() { return words.size(); }

    public String getId() {
        return "cache";
    }
    
}
