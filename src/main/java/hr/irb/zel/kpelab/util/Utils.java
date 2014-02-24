package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.term.WeightedTerm;
import java.util.Collections;
import java.util.List;

/** Various utility methods. */
public class Utils {

    public static void printWeightedTerms(List<WeightedTerm> wterms) {
        Collections.sort(wterms);
        for (WeightedTerm wt : wterms) {
            System.out.println(wt);
        }
    }
    
}
