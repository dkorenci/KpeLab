package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.df.PhraseDocumentFrequency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.uima.UIMAException;

/**
 *
 */
public class TfidfKpextractor implements IKpextractor {

    PhraseDocumentFrequency counter;
    PosRegexPhraseExtractor extractor;
    private int numPhrases;
    private List<TfidfPhrase> tfidfphrases;
    
    public TfidfKpextractor(PosRegexPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, int K) {
        this.counter = counter;
        this.extractor = extractor;
        this.numPhrases = K;
    }
    
    public List<Phrase> extract(KpeDocument doc) throws UIMAException {
        List<Phrase> phrases = extractor.extractPhrases(doc.getText());
        int numWords = PhraseHelper.countWords(doc.getText());        
        // calculate tfidfs 
        tfidfphrases = new ArrayList<TfidfPhrase>();
        for (Phrase ph : phrases) {
            TfidfPhrase tfidfph = new TfidfPhrase();
            tfidfph.phrase = ph;
            int tf = ph.getFrequency();
            int df = counter.countOccurences(ph);
            tfidfph.tfidf = tfIdf(tf, numWords, df, counter.getNumDocuments());
            tfidfphrases.add(tfidfph);
        }        
        // sort phrases by tfidf
        Collections.sort(tfidfphrases);
        // return first K phrases
        List<Phrase> result = new ArrayList<Phrase>();
        if (numPhrases <= 0) return result;
        int cnt = 0; 
        for (TfidfPhrase ph : tfidfphrases) {
            result.add(ph.phrase);
            if (++cnt == numPhrases) break;
        }        
        return result;
    }

    // construct sorted list of tfidfed phrases
    public void printTfidfs() {
        for (TfidfPhrase ph : tfidfphrases) {
            System.out.println(ph.phrase + " " + ph.tfidf);
        }
    }
    
    private static double tfIdf(int tf, int docSize, int df, int numDocs) {
        return (tf/(double)docSize) * -1 * Math.log(df/(double)numDocs);
    }

    public int getNumPhrases() { return numPhrases; }
    public void setNumPhrases(int numPhrases) { this.numPhrases = numPhrases; }
    
    private class TfidfPhrase implements Comparable<TfidfPhrase>{
        Phrase phrase;
        double tfidf;
        // inverse sort by tfidf
        public int compareTo(TfidfPhrase ph) {
            if (this.tfidf > ph.tfidf) return -1;
            else if (this.tfidf < ph.tfidf) return 1;
            else return 0;
        }
    }
    
}
