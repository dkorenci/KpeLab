/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class Phrase implements Comparable<Phrase>, Serializable {
    
    private static final long serialVersionUID = -5522542918925907883L;
    
    private int firstOccurence;
    private int frequency;
    private String canonicStr; // string representation
    
    private List<String> tokens;
    private List<String> ctokens;   

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Phrase == false) return false;
        Phrase ph = (Phrase)o;
        if (ctokens.size() != ph.ctokens.size()) return false;
        for (int i = 0; i < ctokens.size(); ++i) {
            if (ctokens.get(i).equals(ph.ctokens.get(i)) == false)
                return false;
        }
        return true;        
    }
    
    @Override
    public String toString() {        
        String s = tokens.get(0);        
        for (int i = 1; i < tokens.size(); ++i) s = s + " " + tokens.get(i);
        return s;
    }
    
    public String canonicForm() {
        if (canonicStr == null) {
            canonicStr = ctokens.get(0);         
            for (int i = 1; i < ctokens.size(); ++i) 
                canonicStr = canonicStr + " " + ctokens.get(i);
        }
        return canonicStr;
    }    
    
    public int compareTo(Phrase ph) {
        String cthis = this.canonicForm();
        String cph = ph.canonicForm();
        return cthis.compareTo(cph);
    }    
    
    // return true if this is real subphrase of ph
    public boolean isSubphrase(Phrase ph) {
        if (ph.ctokens.size() <= this.ctokens.size()) return false;        
        List<String> subtok = this.ctokens; int SS = subtok.size();
        List<String> tok = ph.ctokens; int S = tok.size();
        for (int i = 0; i < S-SS+1; ++i) {
            boolean match = true; // match at position i in tok
            for (int j = 0; j < SS; ++j) {
                if (subtok.get(j).equals(tok.get(i+j)) == false) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }
    
    // ****************** getters and setters ***********************
    
    public int getFirstOccurence() {
        return firstOccurence;
    }

    public void setFirstOccurence(int firstOccurence) {
        this.firstOccurence = firstOccurence;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getCanonicTokens() {
        return ctokens;
    }

    public void setCanonicTokens(List<String> canonicTokens) {
        this.ctokens = canonicTokens;
        canonicStr = null;
    }
    
}
