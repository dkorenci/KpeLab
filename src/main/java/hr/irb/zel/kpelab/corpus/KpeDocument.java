package hr.irb.zel.kpelab.corpus;

import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;

/** Document data: text, id and solution keyphrases. */
public class KpeDocument {
    
    public KpeDocument() {}
    
    public KpeDocument(String i, String txt, List<Phrase> phrases) {
        id = i; text = txt; keyphrases = phrases;
    }
    
    private String text;
    private List<Phrase> keyphrases;
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
        
    public void setText(String t) { text = t; }
    public String getText() { return text; };

    public List<Phrase> getKeyphrases() { return keyphrases; }
    public void setKeyphrases(List<Phrase> keyphrases) { this.keyphrases = keyphrases; }
}
