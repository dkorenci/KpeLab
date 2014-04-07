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
    public static String doubleStr(double d) {
        return String.format("%.3f", d);
    }
    
    // convert double to string, rounded to prec decimal places 
    public static String doubleStr(double d, int prec) {
        return String.format("%."+prec+"f", d);
    }
    
    // fill string to fixed width with left blanks 
    public static String fixw(String s, int width) { 
        return String.format("%1$"+width+"s", s);
    }
    
    public static <T> List<T> getRandomSubsample(List<T> list, int S) {
        return getRandomSubsample(list, S, 567771);
    }
    
    // get random subsample of size S
    public static <T> List<T> getRandomSubsample(List<T> list, int S, int seed) {
        if (S >= list.size() || S < 0) return list;
        List<T> res = new ArrayList<T>(S);
        if (S == 0) return res;
        Set<Integer> sampled = new TreeSet<Integer>(); // already sampled indexes
        Random r = new Random(seed);
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
    
    // sorts two lists of the same length according to order in the second list
    public static <T, C extends Comparable<? super C>> void sort(
            List<T> list, List<C> order, boolean reverse) {        
        if (list.size() != order.size()) 
            throw new IllegalArgumentException("lists must be of same size");
        int N = list.size();
        // util class for sorting, that groups types from both lists and sorts by 
        // values from second list
        class TC implements Comparable<TC> {
            T t; C c;
            public int compareTo(TC o) {                
                return c.compareTo(o.c);
            }            
        }
        List<TC> clist = new ArrayList<TC>(N);
        for (int i = 0; i < N; ++i) {
            TC e = new TC(); e.t = list.get(i); e.c = order.get(i);
            clist.add(e);
        }
        Collections.sort(clist);
        for (int i = 0; i < N; ++i) {
            list.set(i, clist.get(i).t);
            order.set(i, clist.get(i).c);
        }        
        if (reverse) {
            Collections.reverse(list);
            Collections.reverse(order);
        }
    }

    public static double arithmeticMean(double [] num) {
        double mean = 0;
        for (double d : num) mean += d;
        return mean/num.length;
    }
    
    public static double geometricMean(double [] num) {
        double mean = 1;
        for (double d : num) mean *= d;
        return Math.pow(mean, 1.0/num.length);
    }
    
    public static double harmonicMean(double [] num) {
        double mean = 0;
        for (double d : num) mean += 1/d;
        return num.length / mean;
    }    
    
}
