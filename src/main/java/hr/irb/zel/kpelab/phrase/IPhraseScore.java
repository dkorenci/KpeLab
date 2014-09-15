package hr.irb.zel.kpelab.phrase;

import hr.irb.zel.kpelab.corpus.KpeDocument;

public interface IPhraseScore {
    
    double score(Phrase ph);
    void adaptToText(String text) throws Exception;
    
}
