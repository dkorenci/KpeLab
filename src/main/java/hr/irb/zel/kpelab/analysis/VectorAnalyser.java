
package hr.irb.zel.kpelab.analysis;

import vectors.IRealVector;
import vectors.VectorEntry;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class VectorAnalyser {

    public static void analyse(IWordToVectorMap wvm) throws Exception {                
        Collection<String> words = wvm.getWords();
        Set<Integer> components = new HashSet<Integer>();
        int numWords = words.size();
        int nonZeroEntries = 0;        
        for (String w : words) {
            IRealVector v = wvm.getWordVector(w);
            VectorEntry[] entries = v.getNonZeroEntries();
            nonZeroEntries += entries.length;
            for (VectorEntry e : entries) components.add(e.coordinate);
        }
        System.out.println("num words: " + numWords);
        System.out.println("num concepts: " + components.size());
        System.out.println("non zero entries: " + nonZeroEntries);
        System.out.println("avg. vector size: " + ((double)nonZeroEntries/numWords));
    }
    
}
