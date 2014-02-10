package hr.irb.zel.kpelab.corpus.semeval;

import hr.irb.zel.kpelab.phrase.Phrase;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Map from document id's to a list of solution phrases.
 * Each solution phrase can come in more than one surface form.
 */
public class DocumentToSolutionMap {

    Map<String, List<List<Phrase>> > solutions;
    
    public DocumentToSolutionMap(String mapFile) throws IOException {
        BufferedReader reader =  new BufferedReader(new FileReader(mapFile));
        String line;
        solutions = new TreeMap<String, List<List<Phrase>> >();
        while ((line = reader.readLine()) != null) {
            line = line.trim(); if (line.equals("")) continue;
            int col = line.indexOf(':');
            if (col <= 0) throw new IllegalArgumentException("error with ':' separator");
            String id = line.substring(0, col-1).trim();
            String sol = line.substring(col+1).trim();
            solutions.put(id, parseSolutionString(sol));
        }
        reader.close();
    }
    
    private List<List<Phrase>> parseSolutionString(String solStr) {
        String [] phrases = solStr.split(",");
        List<List<Phrase>> sol = new ArrayList<List<Phrase>>();
        for (String phrase : phrases) {
            List<Phrase> forms = new ArrayList<Phrase>();
            String [] surfForms = phrase.split("\\+");
            for (String sf : surfForms) forms.add(phraseFromString(sf));
            sol.add(forms);
        }
        return sol;
    }
    
    private Phrase phraseFromString(String str) {
        Phrase ph = new Phrase();
        List<String> tokens = Arrays.asList(str.split("\\s"));
        ph.setCanonicTokens(tokens);
        ph.setTokens(tokens);
        return ph;
    }
    
    /** Return solutions, or null id is not in the map. */
    public List<List<Phrase>> getSolutions(String docId) {
        return solutions.get(docId);
    }
    
}
