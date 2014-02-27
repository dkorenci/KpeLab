package hr.irb.zel.kpelab.corpus.semeval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clean document text from non-text elements.
 */
public class DocumentCleaner {
        
    private static Pattern abstractLine;
    static { abstractLine = Pattern.compile("\\s*ABSTRACT\\s*"); }
    private static Pattern referencesLine;
    static { referencesLine = Pattern.compile("\\s*([1-9]{1,2}\\.)?\\s*ACKNOWLEDGMENTS\\s*"); }
    private static Pattern acknowledgeLine;
    static { acknowledgeLine = Pattern.compile("\\s*([1-9]{1,2}\\.)?\\s*REFERENCES\\s*"); }    
    private static Pattern appendixLine;
    static { appendixLine = Pattern.compile("\\s*([1-9]{1,2}\\.)?\\s*APPENDIX\\s*"); }    
    private static Pattern sectTitleLine;
    static { sectTitleLine = Pattern.compile("\\s*([1-9]\\.?)+(\\s*\\p{Alpha}+\\s*)+"); }         
    private static Pattern letter;
    static { letter = Pattern.compile("\\p{Alpha}"); }  
    
    // maknuti sve između naslova i abstracta
    // maknuti sve naslove poglavlja
    // maknuti sve non-alpha retke
    // maknuti sve nakon {N. ACKNOWLEDGMENTS, N. REFERENCES, APPENDIX}, 
    // štogod dođe prvo
    
    public static String cleanDocument(String text) {
        String [] lines = text.split("\n");                
        // set lines to be excluded to null
        for (int i = 0; i < lines.length; ++i) {
            // remove everything between title and "ABSTRACT"
            // title is considered to span first 2 lines
            if (isAbstract(lines[i])) {
                for (int j = 2; j <= i; ++j) { lines[j] = null; }
                continue;
            }
            // remove lines with no letters
            if (!hasLetter(lines[i])) { lines[i] = null; continue; }             
            if (isRef(lines[i]) || isAck(lines[i]) || isAppendix(lines[i])) {
                // remove everything to the end of document
                for (int j = i; j < lines.length; ++j) { lines[j] = null; }
                break;
            }
        }
        // concatenate lines to string
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i] != null) builder.append(lines[i]).append("\n");
        }
        return builder.toString();
    }
    
    public static void analyzeDocument(String text) {
        String [] lines = text.split("\n");        
        for (String line : lines) {            
            if (isAbstract(line)) System.out.println("abstract: " + line);
            if (isAck(line)) System.out.println("ack: " + line);
            if (isAppendix(line)) System.out.println("app: " + line);
            if (isRef(line)) System.out.println("ref: " + line);
            if (!hasLetter(line)) System.out.println("no letters: " + line);
            //if (isSectionTitle(line)) System.out.println("sect. title: " + line);
        }        
    }    
    
    public static boolean isAbstract(String line) {        
        return abstractLine.matcher(line).matches();
    }
    
    public static boolean isRef(String line) {
        return referencesLine.matcher(line).matches();
    }    
    
    public static boolean isAppendix(String line) {
        return appendixLine.matcher(line).matches();
    }    
    
    public static boolean isAck(String line) {
        return acknowledgeLine.matcher(line).matches();
    }
    
    public static boolean isSectionTitle(String line) {
        return sectTitleLine.matcher(line).matches();
    }
    
    public static boolean hasLetter(String line) {
        return letter.matcher(line).find();                
    }
    
}
