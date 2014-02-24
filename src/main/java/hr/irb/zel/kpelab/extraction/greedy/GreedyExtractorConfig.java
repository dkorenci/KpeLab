package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;

/**
 * Processing components of GreedyExtractor and other configuration.
 */
public class GreedyExtractorConfig {
    
    public IDocumentVectorizer docVectorizer;
    public IPhraseSetVectorizer phVectorizer;
    public IVectorComparison phraseSetQuality;
    public IPhraseExtractor phraseExtractor;    

    public GreedyExtractorConfig(IDocumentVectorizer dvec, IPhraseExtractor phext, 
            IPhraseSetVectorizer phvec, IVectorComparison cmp) {    
        docVectorizer = dvec; phVectorizer = phvec; 
        phraseSetQuality = cmp; phraseExtractor = phext;        
    }
    
}
