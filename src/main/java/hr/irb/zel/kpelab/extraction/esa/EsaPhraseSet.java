package hr.irb.zel.kpelab.extraction.esa;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.tabu.ISearchPhraseSet;
import hr.irb.zel.kpelab.extraction.tabu.KpeTabuSearch;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.uima.UIMAException;

/** */
public class EsaPhraseSet implements ISearchPhraseSet {

    IWordToVectorMap wordVectors;
    IRealVector documentVector, phraseVector;
    List<Phrase> documentPhrases;
    List<Phrase> phrases;
    Map<String, Integer> stemCounts;
    
    private static PosRegexPhraseExtractor phExtractor = null;

    public EsaPhraseSet(IWordToVectorMap vec) {
        wordVectors = vec;
    }
        
    public void setDocument(KpeDocument doc) throws Exception {
        extractDocumentPhrases(doc);
        constructDocumentVector();
    }

    // extract list of unique phrases
    private void extractDocumentPhrases(KpeDocument doc) throws UIMAException {
        phExtractor = getExtractor();
        List<Phrase> ph = phExtractor.extractPhrases(doc.getText());
        Set<Phrase> uniquePhrases = new TreeSet<Phrase>(ph);        
        documentPhrases = new ArrayList<Phrase>(uniquePhrases);
//        System.out.println("unique phrases: ");
//        KpeTabuSearch.printPhraseSet(documentPhrases, 7);
    }
    
    private static PosRegexPhraseExtractor getExtractor() throws UIMAException {
        if (phExtractor == null) {
            phExtractor = new PosRegexPhraseExtractor(CanonicForm.STEM);
        }
        return phExtractor;
    }
    
    // sum vectors from all phrase stems, add each unique stem once
    private void constructDocumentVector() throws Exception {
        Set<String> stemSet = new TreeSet<String>();
        documentVector = null;
        for (Phrase ph : documentPhrases) {            
            for (String stem : ph.getCanonicTokens())
            if (!stemSet.contains(stem)) {                
                stemSet.add(stem);                
                if (wordVectors.hasWord(stem)) {
                    IRealVector vec = wordVectors.getWordVector(stem);
                    if (documentVector == null) documentVector = vec.clone();
                    else documentVector.add(vec);                    
                    //System.out.println(stem);
                    //System.out.println(vec);
                    //System.out.println(documentVector);                    
                }
            }            
        }
    }    
    
    public void setPhraseSet(List<Phrase> phraseSet) throws Exception {
        // create map od stem counts
        stemCounts = new TreeMap<String, Integer>();        
        for (Phrase ph : phraseSet) addStemCounts(ph);
        phrases = phraseSet;
        // calculate vector that is a sum of stem vectors
        phraseVector = null;
        for (String stem : stemCounts.keySet()) {
            if (wordVectors.hasWord(stem)) {
                IRealVector vec = wordVectors.getWordVector(stem);
                if (phraseVector == null) phraseVector = vec.clone();
                else phraseVector.add(vec);                    
            }
        }
    }

    private void addStemCounts(Phrase ph) {
        for (String stem : ph.getCanonicTokens()) {
            if (stemCounts.containsKey(stem)) {
                stemCounts.put(stem, stemCounts.get(stem)+1);
            } else stemCounts.put(stem, 1);
        }        
    }
    
    private void subtractStemCounts(Phrase ph) {
        for (String stem : ph.getCanonicTokens()) {
            if (stemCounts.containsKey(stem)) {          
                int cnt = stemCounts.get(stem);
                assert(cnt >= 1);
                if (cnt > 1) stemCounts.put(stem, stemCounts.get(stem)-1);
                else stemCounts.remove(stem);                
            } 
        }        
    }
    
    
    public void replacePhrase(int ind, Phrase newPh) throws Exception {
        List<String> newStems = newPh.getCanonicTokens();
        Phrase oldPh = phrases.get(ind); 
        List<String> oldStems = oldPh.getCanonicTokens();        
        // mark which stems were present before addition and removal
        boolean wasInOld[] = new boolean[oldPh.getCanonicTokens().size()];
        boolean wasInNew[] = new boolean[newPh.getCanonicTokens().size()];
        for (int i = 0; i < oldStems.size(); ++i) {
            String stem = oldStems.get(i);
            if (stemCounts.containsKey(stem)) wasInOld[i] = true;
            else wasInOld[i] = false;
        }
        for (int i = 0; i < newStems.size(); ++i) {
            String stem = newStems.get(i);
            if (stemCounts.containsKey(stem)) wasInNew[i] = true;
            else wasInNew[i] = false;
        }        
        // add new, remove old
        addStemCounts(newPh);
        subtractStemCounts(oldPh);
        // add and subtract vectors of stems that changed their presence
        // in the stem set before addition and removal
        for (int i = 0; i < oldStems.size(); ++i) {
            String stem = oldStems.get(i);
            if (wasInOld[i] && !stemCounts.containsKey(stem)) {
                if (wordVectors.hasWord(stem)) {
                    IRealVector vec = wordVectors.getWordVector(stem);
                    phraseVector.subtract(vec);
                }
            }
        }
        for (int i = 0; i < newStems.size(); ++i) {
            String stem = newStems.get(i);
            if (!wasInNew[i] && stemCounts.containsKey(stem)) {
                if (wordVectors.hasWord(stem)) {
                    IRealVector vec = wordVectors.getWordVector(stem);
                    phraseVector.add(vec);
                }                
            }
        }
        
        phrases.set(ind, newPh);
    }

    public List<Phrase> getDocumentPhrases() {
        return documentPhrases;
    }

    public List<Phrase> getPhrases() {        
        return new ArrayList<Phrase>(phrases);
    }

    public Phrase getPhrase(int i) {
        return phrases.get(i);
    }

    public int numPhrases() {
        return phrases.size();
    }

    public double calculateQuality() {
        return phraseVector.cosine(documentVector);
    }

    public boolean containsPhrase(Phrase ph) {
        return phrases.contains(ph);
    }

    public void printDebugData() {
        System.out.println("document vector: ");
        System.out.println(documentVector);
        System.out.println("phrases vector: ");
        System.out.println(phraseVector);
    }

}
