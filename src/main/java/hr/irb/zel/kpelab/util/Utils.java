package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.term.WeightedTerm;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Various utility methods. */
public class Utils {

    private static final Pattern wordPattern =
        Pattern.compile("\\p{Alpha}+(\\-\\p{Alpha})?");
    
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
    
}
