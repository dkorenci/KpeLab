package hr.irb.zel.kpelab.vectors.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;

public class PhraseVectorizer implements IPhraseVectorizer {

    private IWordToVectorMap wordVectorMap;
    
    public PhraseVectorizer(IWordToVectorMap wvm) {
        this.wordVectorMap = wvm;
    }
    
    public IRealVector phraseToVector(Phrase phrase) throws Exception {
        List<String> words = phrase.getCanonicTokens();
        // check all the words are in the map
        for (String word : words) 
            if (this.wordVectorMap.hasWord(word) == false) return null;
        // construct vector
        IRealVector result = this.wordVectorMap.getWordVector(words.get(0));
        for (int i = 1; i < words.size(); ++i) {
            result = result.ebeMultiply(this.wordVectorMap.getWordVector(words.get(i)));
        }
        return result;
    }
    
    

}
