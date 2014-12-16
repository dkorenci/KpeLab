package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.util.Utils;
import vectors.IRealVector;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
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
    
    private boolean verbose; // produce output
    private String outputFolder;    
    
    /** Initialize with processing components. Comparison must be a 
     * measure of quality of a phrase set for the document, first argument 
     * is a phrase set vector, second is a document vector. */
    public GreedyExtractor(int K, GreedyExtractorConfig conf) {
        c = conf; phraseSetSize = K;
    }

    public String getId() {
        return c.getId();
    }
    
    public void makeVerbose(String outFolder) {
        verbose = true;
        outputFolder = outFolder;
    }
    
    public List<Phrase> extract(String text) throws Exception {
        prepareForExtraction(text);
        // if (verbose) printRankedCandidates();
        constructPhraseSet();
        return phrases;
    }   
    
    // create necessary data structures, it is public to be used
    // before printing ranked phrases
    public void prepareForExtraction(String text) throws Exception {        
        c.adaptToDocument(text);
        documentVector = c.docVectorizer.vectorize(text);
        candidates = c.phraseExtractor.extractPhrases(text);   
        filterCandidates();
        removeNullCandidates();        
    }
    
    // filter out phrases with frequency < 3
    private void filterCandidates() {
        Iterator<Phrase> it = candidates.iterator();
        while (it.hasNext()) {
            Phrase ph = it.next();
            if (ph.getFrequency() < 3 || ph.getFirstOccurence() > 400)
                it.remove();
        }
    }      
    
    private void constructPhraseSet() throws Exception {
//        PrintStream pr = null;
//        if (verbose) {
//            pr = new PrintStream(
//                 new FileOutputStream(outputFolder+document.getId()+".sol.build.txt"));
//        }                
        
        c.phVectorizer.clear();
        phrases = new ArrayList<Phrase>();
        //System.out.println(candidates.size());
        for (int i = 0; i < phraseSetSize; ++i) {
            Phrase optPhrase = null; 
            double optQual = Double.NEGATIVE_INFINITY;               
            for (Phrase ph : candidates) {
            if (!phrases.contains(ph)) {
                c.phVectorizer.addPhrase(ph);
                IRealVector phVec = c.phVectorizer.vector();
                double phQuality = c.phraseSetQuality.compare(phVec, documentVector);
                //System.out.println(phQuality);
                if (phQuality > optQual) {
                    optQual = phQuality;
                    optPhrase = ph;
                }
                c.phVectorizer.removeLastAdded();
            }
            }
            if (optPhrase != null) { 
                phrases.add(optPhrase);
                c.phVectorizer.addPhrase(optPhrase);
            }            
//            if (verbose) { 
//                pr.println("optimum quality: " + Utils.doubleStr(optQual));
//                PhraseHelper.printPhraseSet(pr, phrases, phraseSetSize, false);                
//            }
            //System.out.println("*******************************************");
        }
        
//        if (verbose) pr.close();
    }  
    
    public void printRankedCandidates(PrintStream out) throws Exception {       
        c.phVectorizer.clear();
        List<Phrase> cand = new ArrayList<Phrase>();
        List<Double> cqual = new ArrayList<Double>();
        for (Phrase ph : candidates) {
            c.phVectorizer.addPhrase(ph);
            IRealVector phVec = c.phVectorizer.vector();
            double phQuality = c.phraseSetQuality.compare(phVec, documentVector);
            cand.add(ph); cqual.add(phQuality);
            c.phVectorizer.removeLastAdded();
        }        
        Utils.sort(cand, cqual, true);
        for (int i = 0; i < cand.size(); ++i) {
            out.print(Utils.fixw(Utils.doubleStr(cqual.get(i)), 10) + " ");
            out.println(cand.get(i).canonicForm() + " ; " + cand.get(i).toString());            
        }        
    }
    
    // remove from set of candidates phrases that cannot be vectorized
    private void removeNullCandidates() {
        Iterator<Phrase> it = candidates.iterator();
        while(it.hasNext()) {
            Phrase ph = it.next();
            if (c.phVectorizer.isNull(ph)) {                 
                it.remove();
            }
            //else System.out.println(ph);
        }        
    }        
    
}
