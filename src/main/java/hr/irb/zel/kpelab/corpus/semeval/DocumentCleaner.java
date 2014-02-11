package hr.irb.zel.kpelab.corpus.semeval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clean document text from non-text elements.
 */
public class DocumentCleaner {
    
    Matcher m;
    private static Pattern abstractLine;
    { abstractLine = Pattern.compile("\\s*ABSTRACT\\s*"); }
    
    public static String cleanDocument(String text) {
        return null;
    }
    
    public static boolean isLineAbstract(String line) {
        return abstractLine.matcher(line).matches();
    }
    
}
