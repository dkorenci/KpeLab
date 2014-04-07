package hr.irb.zel.kpelab.extraction.ranking;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.SubphraseRemover;
import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class KpMinerExtractor implements IKpextractor {

    IPhraseExtractor phext;
    TermDocumentFrequency tdf;
    double boost; // boosting factor (idf approximation for phrases)
    double s, t; //s = 2.3, t = 3; // parameters for calculating boost
    List<Phrase> phrases;
    int N; // number of phrases to be extracted
    
    // construct with default s and t values
    public KpMinerExtractor(IPhraseExtractor extr, TermDocumentFrequency df, int n) {
        this(extr, df, n, 2.3, 3);
    }
    
    public KpMinerExtractor(IPhraseExtractor extr, TermDocumentFrequency df, int n, 
            double ss, double tt) {
        phext = new SubphraseRemover(extr); tdf = df; N = n;
        s = ss; t = tt;
    }

    public String getId() { return "kpminer"; }    
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        phrases = phext.extractPhrases(doc.getText());
        filterPhrases();
        calcBoostingFactor();
        sortPhrases();
        rerankPhrases();        
        int m = N; if (m > phrases.size()) m = phrases.size();
        return phrases.subList(0, m);
    }

    // filter out phrases witf frequency < 3
    private void filterPhrases() {
        Iterator<Phrase> it = phrases.iterator();
        while (it.hasNext()) {
            Phrase ph = it.next();
            if (ph.getFrequency() < 3 || ph.getFirstOccurence() > 400) 
                it.remove();
        }
    }
    
    private void calcBoostingFactor() {
        double numPhrases = phrases.size();
        double numCompound = 0; // phrases with > 1 words
        for (Phrase ph : phrases) {
            if (ph.getCanonicTokens().size() > 1) numCompound++;
        }
        if (numCompound != 0) {
            boost = numPhrases/(numCompound*s);
            if (boost > t) boost = t;
        }
        else boost = 0;
        System.out.println("N: " + numPhrases + " C: " + numCompound);
    }

    // sort phrases by measure of keyness
    private void sortPhrases() {
        List<Double> keyness = new ArrayList<Double>(phrases.size());
        for (Phrase ph : phrases) {
            keyness.add(calcKeyness(ph));
        }
        Utils.sort(phrases, keyness, true);
    }

    // measure for ranking phrases
    private Double calcKeyness(Phrase ph) {
        double k;
        if (ph.getCanonicTokens().size() > 1) {
            double idf = tdf.getNumDocuments() / 1.0;
            idf = Math.log(idf)/Math.log(2); 
            k = ph.getFrequency() * idf * boost;            
        }
        else {
            String word = ph.getCanonicTokens().get(0);
            double df = tdf.documentFrequency(word);
            if (df == 0) df = 1;            
            double idf = tdf.getNumDocuments()/df;
            idf = Math.log(idf)/Math.log(2); // log_2(idf)
            k = ph.getFrequency() * idf;
            //System.out.println("idf: " + idf);
        }
        return k;
    }

    private void rerankPhrases() {
        
    }

}
