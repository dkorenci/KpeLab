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
import hr.irb.zel.kpelab.phrase.SubphraseRemover;
import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** My version of ranking extractor.  */
public class RankerExtractor implements IKpextractor, IPhraseScore  {

    IPhraseExtractor phext;
    TermDocumentFrequency tdf;
    List<Phrase> phrases;
    Map<Phrase, Phrase> subphrase;
    Map<Phrase, Phrase> superphrase;
    int N; // number of phrases to be extracted
    
    public enum Mean { ARITHMETIC, GEOMETRIC, HARMONIC }
    
    public RankerExtractor(IPhraseExtractor extr, TermDocumentFrequency df, int n) {
        phext = new SubphraseRemover(extr); 
        tdf = df; N = n;
    }

    public String getId() { return "ranker"; }    
    
    public List<Phrase> extract(String text) throws Exception {
        phrases = phext.extractPhrases(text); 
        //calcAvgFreq();
        //filterPhrases();
        //clearSubphrases();
        sortPhrases();
        rerankPhrases();
        int m = Math.min(N, phrases.size());
        return phrases.subList(0, m);
    }
    
    public void calcAvgFreq() {
        double avgFreq = 0;
        for (Phrase ph : phrases) {
            avgFreq += (((double)ph.getFrequency()) * ph.getCanonicTokens().size());
        }
        avgFreq /= phrases.size();
        System.out.print("avg freq: " + Utils.doubleStr(avgFreq) + " ");
    }
    
    public double score(Phrase ph) {
        //return ph.getFrequency() * phraseIdf(ph);
        return calcKeyness(ph);
    }    
    
    public void adaptToText(String text) {}
    
    // filter out phrases with frequency < 3
    private void filterPhrases() {
        Iterator<Phrase> it = phrases.iterator();
        while (it.hasNext()) {
            Phrase ph = it.next();
            if (ph.getFrequency() < 3 || ph.getFirstOccurence() > 400) // 
                it.remove();
        }
    }    

    private void clearSubphrases() {
        // integrirati frekvencije podfraza u nadfraze, uz ovo?
        subphrase = new TreeMap<Phrase, Phrase>();
        superphrase = new TreeMap<Phrase, Phrase>();
        for (Phrase ph1 : phrases) {
            for (Phrase ph2 : phrases) {
                if (ph1.isSubphrase(ph2)) {
                    Phrase sup1 = superphrase.get(ph1);
                    Phrase sub2 = subphrase.get(ph2);
                    if (sup1 == null || sup1.getCanonicTokens().size() > 
                            ph2.getCanonicTokens().size()) {
                        superphrase.put(ph1, ph2);
                    }
                    if (sub2 == null || sub2.getCanonicTokens().size() < 
                            ph1.getCanonicTokens().size()) {
                        subphrase.put(ph2, ph1);
                    }                    
                }
            }
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
        double k = 1.0;
        k *= ph.getFrequency();
        k *= phraseIdf(ph);
        k *= Math.log(ph.getCanonicTokens().size());   
        k /= Math.log(ph.getFirstOccurence()+1);
//        k *= ph.getFrequency();
//        k *= phraseIdf(ph);
//        k *= Math.log(ph.getCanonicTokens().size()+0.1);
//        k *= Math.pow(ph.getRelFirstOccurence(), 3);
        return k;         
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
