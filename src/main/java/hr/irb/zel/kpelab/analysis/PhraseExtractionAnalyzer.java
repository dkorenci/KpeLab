package hr.irb.zel.kpelab.analysis;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;


public class PhraseExtractionAnalyzer {
    
    IPhraseExtractor extractor;
    
    public PhraseExtractionAnalyzer(IPhraseExtractor extr) {
        extractor = extr;
    }
    
    public void printPhrases(KpeDocument doc) throws Exception {
        System.out.println("phrases for document " + doc.getId());
        for (Phrase ph : extractor.extractPhrases(doc.getText())) {
            System.out.println(ph.toString());
        }
    }
    
}
