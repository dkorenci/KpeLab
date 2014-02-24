package hr.irb.zel.kpelab.evaluation;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import java.util.List;

/** Phrases are equal if solution phrase tokens stemmed by standard Porter stemmer
 * match solution phrases (which are already stemmed). */
public class SemevalPhraseEquality implements IPhraseEquality {

    public boolean equal(Phrase solution, Phrase correct) {
        List<String> solTok = solution.getTokens();
        List<String> corrCanonTok = correct.getCanonicTokens();
        if (solTok.size() != corrCanonTok.size()) return false;
        for (int i = 0; i < solTok.size(); ++i) {
            String s =  solTok.get(i).toLowerCase();
            if (s.contains("-")) {
                // stem individually each of the tokens seperated by hyphen
                String [] parts = s.split("\\-");                
                s = "";
                for (int j = 0; j < parts.length; ++j) {
                    s = s + PhraseHelper.stemWordPorter(parts[j]);
                    if (j < parts.length - 1) s = s + "-";
                }
            }
            else s = PhraseHelper.stemWordPorter(s);            
            if (s.equals(corrCanonTok.get(i).toLowerCase()) == false) return false;                        
        }
        return true;
    }
    
}
