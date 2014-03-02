package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.VectorEntry;

/** Coverts vector from the map to 0-1 vectors that have 
 * value of 1 at all coordinates !=0. */
public class WordToVector01Filter implements IWordToVectorMap {

    IWordToVectorMap wordToVector;
    WordToVectorMapCache cache;
    private static final int CACHE_SIZE = 10000;
    
    public WordToVector01Filter(IWordToVectorMap wvm) { 
        wordToVector = wvm; 
        cache = new WordToVectorMapCache(CACHE_SIZE);
    }
    
    public String getId() { return wordToVector.getId()+"01"; }
    
    public IRealVector getWordVector(String word) throws Exception {
        if (cache.hasWord(word)) return cache.getWordVector(word);
        else {
            if (wordToVector.hasWord(word)) {
                IRealVector vec = wordToVector.getWordVector(word);
                IRealVector vec01 = create01Vector(vec);
                cache.addWordVectorPair(word, vec01);
                return vec01;
            }
            else return null;
        }
    }

    public boolean hasWord(String word) {
        return wordToVector.hasWord(word);
    }

    private IRealVector create01Vector(IRealVector v) {
        VectorEntry[] entries = v.getNonZeroEntries(); 
        IRealVector result = v.clone();
        for (VectorEntry e : entries) {
            result.setElement(e.coordinate, 1);
        }
        return result;
    }
    
}
