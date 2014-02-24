package hr.irb.zel.kpelab.evaluation;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import java.util.List;

/**
 * Two phrases are equal if their cannonic tokens are equal.
 */
public class CanonicPhraseEquality implements IPhraseEquality {

    public boolean equal(Phrase solution, Phrase correct) {
        List<String> solTok = solution.getCanonicTokens();
        List<String> corrTok = correct.getCanonicTokens();
        if (solTok.size() != corrTok.size()) return false;
        for (int i = 0; i < solTok.size(); ++i) {
            String s =  solTok.get(i).toLowerCase();            
            if (s.equals(corrTok.get(i).toLowerCase()) == false) return false;            
        }
        return true;        
    }

}
