package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.TermSetPruneFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Generic greedy extractor.
 */
public class GreedyExtractor implements IKpextractor {

    private final int phraseSetSize;   
    private final GreedyExtractorConfig c;
    
    private IRealVector documentVector;
    private List<Phrase> candidates;
    private List<Phrase> phrases; // result        
    
    /** Initialize with processing components. Comparison must be a 
     * measure of quality of a phrase set for the document, first argument 
     * is a phrase set vector, second is a document vector. */
    public GreedyExtractor(int K, GreedyExtractorConfig conf) {
        c = conf; phraseSetSize = K;
    }

    public String getId() {
        return c.getId();
    }
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        c.adaptToDocument(doc.getText());
        documentVector = c.docVectorizer.vectorize(doc.getText());
        candidates = c.phraseExtractor.extractPhrases(doc.getText());     
        System.out.println("numCandidates: "+candidates.size());
        removeNullCandidates();
        constructPhraseSet();
        return phrases;
    }   
    
    private void constructPhraseSet() throws Exception {
        c.phVectorizer.clear();
        phrases = new ArrayList<Phrase>();
        System.out.println(candidates.size());
        for (int i = 0; i < phraseSetSize; ++i) {
            Phrase optPhrase = null; 
            double optQual = Double.NEGATIVE_INFINITY;               
            for (Phrase ph : candidates) {
            if (!phrases.contains(ph)) {
                c.phVectorizer.addPhrase(ph);
                IRealVector phVec = c.phVectorizer.vector();
                double phQuality = c.phraseSetQuality.compare(phVec, documentVector);
                //System.out.println(phQuality);
                if (phQuality > optQual) {
                    optQual = phQuality;
                    optPhrase = ph;
                }
                c.phVectorizer.removeLastAdded();
            }
            }
            if (optPhrase != null) { 
                phrases.add(optPhrase);
                c.phVectorizer.addPhrase(optPhrase);
            }            
            //System.out.println("*******************************************");
        }
    }

    // remove from set of candidates phrases that cannot be vectorized
    private void removeNullCandidates() {
        Iterator<Phrase> it = candidates.iterator();
        while(it.hasNext()) {
            Phrase ph = it.next();
            if (c.phVectorizer.isNull(ph)) {                 
                it.remove();
            }
            //else System.out.println(ph);
        }        
    }        
    
}
