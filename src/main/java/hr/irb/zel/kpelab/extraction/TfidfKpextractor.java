package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.df.PhraseDocumentFrequency;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.tfidf.ITfidfCalculator;
import hr.irb.zel.kpelab.tfidf.TfidfCalculator;
import hr.irb.zel.kpelab.util.EnglishWordCounter;
import hr.irb.zel.kpelab.util.IWordCounter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.uima.UIMAException;

/**
 *
 */
public class TfidfKpextractor implements IKpextractor {

    PhraseDocumentFrequency counter;
    IPhraseExtractor extractor;
    IWordCounter wordCounter;
    ITfidfCalculator tfIdf;
    private int numPhrases;
    private List<TfidfPhrase> tfidfphrases;
    
    public TfidfKpextractor(IPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, int K) {
        configure(extractor, counter, K, new EnglishWordCounter(), new TfidfCalculator());
    }
    
    public TfidfKpextractor(IPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, int K, ITfidfCalculator tfidf) {
        configure(extractor, counter, K, new EnglishWordCounter(), tfidf);
    }
        
    public TfidfKpextractor(IPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, IWordCounter wcounter, int K) {
        configure(extractor, counter, K, wcounter, new TfidfCalculator());
    }    

    public TfidfKpextractor(IPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, IWordCounter wcounter, 
            int K, ITfidfCalculator tfidf) {
        configure(extractor, counter, K, wcounter, tfidf);
    }          
    
    // configure object with given components
    private void configure(IPhraseExtractor extractor, 
            PhraseDocumentFrequency counter, int K, IWordCounter wcounter, ITfidfCalculator tfidf) {
        this.extractor = extractor;
        this.counter = counter;        
        this.numPhrases = K; 
        this.wordCounter = wcounter;
        this.tfIdf = tfidf;
    }
    
    public String getId() { return "tfidf"; }
    
    public List<Phrase> extract(String text) throws Exception {
        List<Phrase> phrases = extractor.extractPhrases(text);
        int numWords = wordCounter.countWords(text);
        // calculate tfidfs 
        tfidfphrases = new ArrayList<TfidfPhrase>();
        for (Phrase ph : phrases) {
            TfidfPhrase tfidfph = new TfidfPhrase();
            tfidfph.phrase = ph;
            int tf = ph.getFrequency();
            int df = counter.countOccurences(ph);
            tfidfph.tfidf =  tfIdf.tfIdf(tf, numWords, df, counter.getNumDocuments());
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
            String data = String.format("phrase %s ; tfidf %.3f ; tf %d ; df %d  ", 
             ph.phrase, ph.tfidf, ph.phrase.getFrequency(), counter.countOccurences(ph.phrase));
            System.out.println(data);
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
