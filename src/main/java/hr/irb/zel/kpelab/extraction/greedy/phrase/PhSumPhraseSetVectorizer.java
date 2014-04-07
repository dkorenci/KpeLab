package hr.irb.zel.kpelab.extraction.greedy.phrase;

import hr.irb.zel.kpelab.phrase.IPhraseScore;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.Map;
import java.util.TreeMap;

public class PhSumPhraseSetVectorizer implements IPhraseSetVectorizer {

    private IWordToVectorMap wordToVector;
        
    private IRealVector vector; // current vector    
    private Map<Phrase, IRealVector> phraseVectors; // vectors of phrasess added to the set
    private IRealVector lastAdded;
    private IPhraseScore phScore;
    
    public PhSumPhraseSetVectorizer(IWordToVectorMap wvm, IPhraseScore scr) {
        wordToVector = wvm;
        phraseVectors = new TreeMap<Phrase, IRealVector>();
        phScore = scr;
    }
    
    public String getId() { return "phsum"; }    
    
    public void setVectors(IWordToVectorMap wvmap) {
        wordToVector = wvmap;
    }

    public void addPhrase(Phrase ph) throws Exception {
        IRealVector phVec = getPhraseVector(ph);
        if (vector == null) vector = phVec.clone();
        else vector.add(phVec);        
        lastAdded = phVec;
    }

    private IRealVector getPhraseVector(Phrase ph) throws Exception {
        if (phraseVectors.containsKey(ph)) return phraseVectors.get(ph);
        IRealVector vec = null;
        for (String w : ph.getCanonicTokens()) {
            IRealVector v = wordToVector.getWordVector(w);
            if (v == null) continue;
            v = v.clone();
            if (phScore != null) v.multiply(phScore.score(ph));
            if (vec == null) vec = v;                               
            else vec.add(v);              
        }
        //vec.multiply(1.0/(ph.getCanonicTokens().size()));
        phraseVectors.put(ph, vec);
        return vec;
    }
    
    public void removeLastAdded() throws Exception {
        if (lastAdded == null) return;
        vector.subtract(lastAdded);
    }

    public IRealVector vector() throws Exception {
        return vector;
    }

    public void clear() {
        vector = null; lastAdded = null; 
        phraseVectors.clear();
    }

    public boolean isNull(Phrase ph) {
        for (String w : ph.getCanonicTokens()) {
            if (wordToVector.hasWord(w)) return false;
        }
        return true;
    }

}
