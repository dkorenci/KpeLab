/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import java.util.Iterator;
import java.util.List;

/**
 * Wraps another IPhraseExtractor and removes from its phrase list
 * those phrases that occur only as a part of a superphrase.
 */
public class SubphraseRemover implements IPhraseExtractor {

    private IPhraseExtractor phExtr;
    
    public SubphraseRemover(IPhraseExtractor ext) { phExtr = ext; }
    
    public List<Phrase> extractPhrases(String text) throws Exception {
        List<Phrase> phrases = phExtr.extractPhrases(text);
        Iterator<Phrase> it = phrases.iterator();
        while (it.hasNext()) {
            Phrase ph = it.next();
            boolean remove = false;
            // remove phrase if there is a superphrase with equal frequency
            for (Phrase phr : phrases) {
                if (ph.isSubphrase(phr)) {
                    //assert(ph.getFrequency() >= phr.getFrequency());                    
                    if (ph.getFrequency() == phr.getFrequency()) {
                        remove = true;
                        break;
                    }                    
                }
            }
            if (remove) it.remove();
        }        
        return phrases;
    }

    public String getId() {
        return phExtr.getId()+"subphRemove";
    }

    
    
}
