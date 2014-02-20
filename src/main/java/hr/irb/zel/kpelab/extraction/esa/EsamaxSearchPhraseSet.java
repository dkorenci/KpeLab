package hr.irb.zel.kpelab.extraction.esa;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.tabu.ISearchPhraseSet;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.SparseRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Phrase set whose quality is measured by coverage of 
 * document vector constructed as max-agregation of esa vectors of terms.
 */
public class EsamaxSearchPhraseSet implements ISearchPhraseSet {
        
    IWordToVectorMap esaVectors;
    List<Phrase> phraseSet; // solution set
    List<Phrase> docPhrases;
    SparseRealVector docVector;
    SparseRealVector phrasesVector;
    double docVectorSum; // sum od docVector coordinates
    
    PosExtractorConfig config;
    KpeDocument doc;
    
    public EsamaxSearchPhraseSet() throws Exception {        
        esaVectors = WordVectorMapFactory.getESAVectors();     
        config = new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM);        
    }
    
    public void setDocument(KpeDocument d) throws Exception {
        doc = d;
        extractDocumentPhrases();
        constructDocumentVector();
    }

    public void setPhraseSet(List<Phrase> phSet) throws Exception {
        phraseSet = phSet;
    }

    public void replacePhrase(int i, Phrase ph) {
        phraseSet.set(i, ph);
    }

    // construct max. aggregated vector od phrase stems
    private void constructPhrasesVector() throws Exception {
        Set<String> phraseStems = new HashSet<String>(phraseSet.size()*3);
        for (Phrase p : phraseSet) phraseStems.addAll(p.getCanonicTokens());
        phrasesVector = maxAggregateTermVectors(phraseStems);        
    }
    
    public List<Phrase> getDocumentPhrases() {
        return docPhrases;
    }

    public List<Phrase> getPhrases() {
        return new ArrayList<Phrase>(phraseSet);
    }

    public Phrase getPhrase(int i) {
        return phraseSet.get(i);
    }

    public int numPhrases() {
        return phraseSet.size();
    }

    public boolean containsPhrase(Phrase ph) {
        return phraseSet.contains(ph);
    }

    public double calculateQuality() throws Exception {
        constructPhrasesVector();
        return docVector.sumMinShared(phrasesVector) / docVectorSum;        
    }

    public void printDebugData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // max. aggregate esa vectors of important document terms
    private void constructDocumentVector() throws Exception {
        TermExtractor extractor = new TermExtractor(config);
        List<String> terms = extractor.extract(doc.getText());
        docVector = maxAggregateTermVectors(terms);
        docVectorSum = docVector.sumOfCoordinates();
    }

    private SparseRealVector maxAggregateTermVectors(Collection<String> terms) 
            throws Exception {        
        SparseRealVector vector = null;
        for (String term : terms) {
            if (esaVectors.hasWord(term)) {
                if (vector == null) 
                    vector = (SparseRealVector) esaVectors.getWordVector(term).clone();
                else 
                    vector.maxMerge(esaVectors.getWordVector(term));                
            }
        }
        if (vector == null) vector = new SparseRealVector();        
        return vector;
    }
    
    private void extractDocumentPhrases() throws UIMAException {
        PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(config);
        docPhrases = extractor.extractPhrases(doc.getText());
    }

}
