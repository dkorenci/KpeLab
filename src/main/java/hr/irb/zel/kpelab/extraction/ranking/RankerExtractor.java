/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction.ranking;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.IPhraseScore;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** My version of ranking extractor.  */
public class RankerExtractor implements IKpextractor, IPhraseScore  {

    IPhraseExtractor phext;
    TermDocumentFrequency tdf;
    List<Phrase> phrases;
    int N; // number of phrases to be extracted
    
    public enum Mean { ARITHMETIC, GEOMETRIC, HARMONIC }
    
    public RankerExtractor(IPhraseExtractor extr, TermDocumentFrequency df, int n) {
        phext = extr; tdf = df; N = n;
    }

    public String getId() { return "ranker"; }    
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        phrases = phext.extractPhrases(doc.getText()); 
        filterPhrases();
        sortPhrases();
        rerankPhrases();
        int m = Math.min(N, phrases.size());
        return phrases.subList(0, m);
    }

    public double score(Phrase ph) {
        //return ph.getFrequency() * phraseIdf(ph);
        return calcKeyness(ph);
    }    
    
    // filter out phrases with frequency < 3
    private void filterPhrases() {
        Iterator<Phrase> it = phrases.iterator();
        while (it.hasNext()) {
            Phrase ph = it.next();
            if (ph.getFrequency() < 3 || ph.getFirstOccurence() > 400)
                it.remove();
        }
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
        return ph.getFrequency() * phraseIdf(ph) * Math.log(ph.getCanonicTokens().size());
    }

    private double phraseIdf(Phrase ph) {
        int size = ph.getCanonicTokens().size();
        if (size == 1) return wordIdf(ph.getCanonicTokens().get(0));
        double [] idfs = new double[size]; int i = 0;
        for (String w : ph.getCanonicTokens()) {
            idfs[i++] = wordIdf(w);
        }
        return Utils.arithmeticMean(idfs);
        //return geometricMean(idfs);
        //return harmonicMean(idfs);
    }
    

    
    private double wordIdf(String word) {
        double df = tdf.documentFrequency(word);        
        double idf = tdf.getNumDocuments()/(df+1);
        idf = Math.log(idf)/Math.log(2); // log_2(idf)
        return idf;       
    }
    
    private void rerankPhrases() {
        
    }

}
