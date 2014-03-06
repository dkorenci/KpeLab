package hr.irb.zel.kpelab.extraction.greedy;

import edu.stanford.nlp.io.PrintFile;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig.VectorMod;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.TermSetPruneFilter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

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
    private KpeDocument document;
    
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
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        document = doc;
        c.adaptToDocument(doc.getText());
        documentVector = c.docVectorizer.vectorize(doc.getText());
        candidates = c.phraseExtractor.extractPhrases(doc.getText());     
        System.out.println("numCandidates: "+candidates.size());
        removeNullCandidates();
        if (verbose) printRankedCandidates();
        constructPhraseSet();
        return phrases;
    }   
    
    private void constructPhraseSet() throws Exception {
        PrintStream pr = null;
        if (verbose) {
            pr = new PrintStream(
                 new FileOutputStream(outputFolder+document.getId()+".sol.build.txt"));
        }                
        
        c.phVectorizer.clear();
        phrases = new ArrayList<Phrase>();
        System.out.println(candidates.size());
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
            if (verbose) { 
                pr.println("optimum quality: " + Utils.doubleStr(optQual));
                PhraseHelper.printPhraseSet(pr, phrases, phraseSetSize);                
            }
            //System.out.println("*******************************************");
        }
        
        if (verbose) pr.close();
    }

    private void printRankedCandidates() throws Exception {
        BufferedWriter w = new BufferedWriter(
                new FileWriter(outputFolder+document.getId()+".cand.rank.txt"));
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
            w.write(Utils.fixw(Utils.doubleStr(cqual.get(i)), 10) + " ");
            w.write(cand.get(i).canonicForm() + " ; " + cand.get(i).toString());
            w.write("\n");            
        }
        w.close();
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
