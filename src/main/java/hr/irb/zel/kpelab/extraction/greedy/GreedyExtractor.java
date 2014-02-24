package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import java.util.ArrayList;
import java.util.List;

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

    public List<Phrase> extract(KpeDocument doc) throws Exception {
        documentVector = c.docVectorizer.vectorize(doc.getText());
        candidates = c.phraseExtractor.extractPhrases(doc.getText());
        constructPhraseSet();
        return phrases;
    }

    private void constructPhraseSet() throws Exception {
        phrases = new ArrayList<Phrase>();
        for (int i = 0; i < phraseSetSize; ++i) {
            //System.out.println(i);
            Phrase optPhrase = null; 
            double optQual = Double.MIN_VALUE;               
            for (Phrase ph : candidates)
            if (!phrases.contains(ph)) {
                c.phVectorizer.addPhrase(ph);
                IRealVector phVec = c.phVectorizer.vector();
                double phQuality = c.phraseSetQuality.compare(phVec, documentVector);
                if (phQuality > optQual) {
                    optQual = phQuality;
                    optPhrase = ph;
                }
                c.phVectorizer.removeLastAdded();
            }
            if (optPhrase != null) phrases.add(optPhrase);
        }
    }
        
    
}
