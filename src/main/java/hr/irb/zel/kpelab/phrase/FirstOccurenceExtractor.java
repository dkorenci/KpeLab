/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.phrase;

import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters phrases output by another phrase extractor based on 
 * the first phrase occurrence data.
 * It filters out phrases that first occur after p% positions
 * (position is ordinal of the token)
 */
public class FirstOccurenceExtractor implements IPhraseExtractor {
    
    IPhraseExtractor extractor;
    double posPerc;
    
    public FirstOccurenceExtractor(IPhraseExtractor extr, double per) {
        extractor = extr; posPerc = per;
    }    

    public List<Phrase> extractPhrases(String text) throws Exception {
        List<Phrase> phrases = extractor.extractPhrases(text);        
        // get max occurence position
        int maxPos = -1;
        for (Phrase ph : phrases) 
            if (ph.getFirstOccurence() > maxPos) maxPos = ph.getFirstOccurence();
        // filter out phrases
        int threshold = (int)(maxPos * posPerc);
        List<Phrase> result = new ArrayList<Phrase>((int)(phrases.size() * posPerc) + 10);        
        for (Phrase ph : phrases) {
            if (ph.getFirstOccurence() <= threshold) result.add(ph);
        }
        
        return result;
    }

    public String getId() {
        return extractor.getId()+Utils.doubleStr(posPerc, 2);
    }

    
    
}
