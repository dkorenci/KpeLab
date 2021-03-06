package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;

/**
 * Extractor that simply returns all the candidate phrases constructed
 * by IPhraseExtractor component.
 * Recall of this extractor is measure of generated candidate quality
 * and theoretical limit for recall of more sophisticated algorithms.
 */
public class AllCandidatesExtractor implements IKpextractor {

    IPhraseExtractor extractor;
    
    public AllCandidatesExtractor(IPhraseExtractor ext) { extractor = ext; }
    
    public String getId() { return "allCand"+extractor.getId(); }
    
    public List<Phrase> extract(String text) throws Exception {
        return extractor.extractPhrases(text);
    }    
    
}
