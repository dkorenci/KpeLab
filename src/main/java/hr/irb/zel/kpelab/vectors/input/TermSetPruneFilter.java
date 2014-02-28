package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.VectorEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Sets to 0 those coordinates that are not shared by at 
 * least one coordinate of a vector from a term set. */
public class TermSetPruneFilter implements IWordToVectorMap {

    private IWordToVectorMap wordToVector;
    private WordToVectorMapCache cache;
    private static final int CACHE_SIZE = 10000;    
    private List<String> terms;
    private Set<Integer> sharedCoords;
    
    public TermSetPruneFilter(IWordToVectorMap wvmap, List<String> t) throws Exception {
        wordToVector = wvmap; 
        cache = new WordToVectorMapCache(CACHE_SIZE);        
        terms = t;
        createCoordFreqs();
    }
    
    private void createCoordFreqs() throws Exception {
        Map<Integer, Integer> coordFreq = new TreeMap<Integer, Integer>();
        for (String t : terms) {
        if (wordToVector.hasWord(t)) {
            IRealVector v = wordToVector.getWordVector(t);
            VectorEntry[] ent = v.getNonZeroEntries();
            for (VectorEntry e : ent) {
                int c = e.coordinate;
                if (!coordFreq.containsKey(c)) coordFreq.put(c, 1); 
                else coordFreq.put(c, coordFreq.get(c)+1);                    
            }
        }
        }
        sharedCoords = new TreeSet<Integer>();
        for (Entry<Integer,Integer> e : coordFreq.entrySet()) {
            if (e.getValue() > 1) sharedCoords.add(e.getKey());
        }
    }
    
    public IRealVector getWordVector(String word) throws Exception {
        if (cache.hasWord(word)) return cache.getWordVector(word);
        else {
            if (wordToVector.hasWord(word)) {
                IRealVector vec = wordToVector.getWordVector(word);
                IRealVector pvec = createPrunedVector(vec);
                cache.addWordVectorPair(word, pvec);
                return pvec;
            }
            else return null;
        }
    }

    public boolean hasWord(String word) {
        return wordToVector.hasWord(word);
    }

    private IRealVector createPrunedVector(IRealVector vec) {
        IRealVector v = vec.clone();
        for (VectorEntry e : vec.getNonZeroEntries()) {
            if (sharedCoords.contains(e.coordinate) == false) {
                v.setElement(e.coordinate, 0);
            }
        }
        return v;
    }

}
