package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.term.WeightedTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Various utility methods. */
public class Utils {

    // either a sequence of alphabetic characters or sequence of chunks
    // separated by a hyphen, where each chunk is a sequence of 
    // alphabetic characters of length at least two
    private static final Pattern wordPattern =
        Pattern.compile("(\\p{Alpha}+|\\p{Alpha}{2,}(\\-\\p{Alpha}{2,})+)");
    
    public static boolean isWord(String token) {
        return wordPattern.matcher(token).matches();
    }
    
    public static void printWeightedTerms(List<WeightedTerm> wterms) {
        Collections.sort(wterms);
        for (WeightedTerm wt : wterms) {
            System.out.println(wt);
        }
    }
    
    // convert double to string, rounded to 3 decimal places 
    public static String doubleToString(double d) {
        return String.format("%.3f", d);
    }
    
    // get random subsample of size S
    public static <T> List<T> getRandomSubsample(List<T> list, int S) {
        if (S >= list.size() || S < 0) return list;
        List<T> res = new ArrayList<T>(S);
        if (S == 0) return res;
        Set<Integer> sampled = new TreeSet<Integer>(); // already sampled indexes
        Random r = new Random(567771);
        for (int i = 0; i < S; ++i) {
            int s;
            do {
                s = r.nextInt(list.size());
            } while (sampled.contains(s));
            res.add(list.get(s));
            sampled.add(s);
        }
        return res;
    }
    
}
