package hr.irb.zel.kpelab.inspector;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.evaluation.F1Evaluator;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.util.Utils;
import vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Output relevant kpe data for a document.  */
public class KpeInspector {
   
    private String baseFolder, outFolder;
    private KpeDocument doc;
    private IRealVector docVector;
    private boolean folderInitialized = false;
    private List<List<Phrase>> subsets;
    private List<Phrase> subset;
    
    public KpeInspector(KpeDocument d) {
        doc = d;
        baseFolder = KpeConfig.getProperty("inspect.output");        
    }
        
    private void initFolder() throws IOException {
        if (folderInitialized) return;
        outFolder = baseFolder + doc.getId()+"/";
        File folder = new File(outFolder);
        if (!folder.exists()) { // delete files
            folder.mkdir();
        }
        else { // create folder
            //for (File f : folder.listFiles()) f.delete();            
        }
        folderInitialized = true;
    }

    // output all candidate phrases, sorted by similarity in descending order
    public void phrasesBySimilarity(GreedyExtractorConfig config) throws Exception {
        initFolder();
        GreedyExtractor extr = new GreedyExtractor(5, config);
        extr.prepareForExtraction(doc.getText());
        PrintStream out = new PrintStream(outFolder+"phrasesBySim.txt");
        out.println("solution phrases: ");
        PhraseHelper.printPhraseSet(out, doc.getKeyphrases(), 5, true);
        out.println("phrases by similarity: ");
        extr.printRankedCandidates(out);
        out.close();
    }

    public void phrasesByFrequency(IPhraseExtractor extr) throws Exception {
        initFolder();
        List<Phrase> phrases = extr.extractPhrases(doc.getText());
        List<Integer> freqs = new ArrayList<Integer>(); 
        for (Phrase ph: phrases) freqs.add(ph.getFrequency());
        Utils.sort(phrases, freqs, true);
        PrintStream out = new PrintStream(outFolder+"phrasesByFreq.txt");
        for (Phrase ph: phrases) {
            out.println(ph.getFrequency() + " | " + ph.canonicForm() + " | " + ph.toString());
        }
        out.close();        
    }
    
    public void extractedPhrases(IKpextractor extr) throws Exception {
        initFolder();
        List<Phrase> kphrases = extr.extract(doc.getText());
        PrintStream out = new PrintStream(outFolder+"keyphrases_"+extr.getId()+".txt");
        
        out.println("correct phrases:");
        PhraseHelper.printPhraseSet(out, doc.getKeyphrases(), 5, true);
        out.println();
        
        out.println("solution phrases:");
        PhraseHelper.printPhraseSet(out, kphrases, 5, true);        
        out.println();
        
        // construct found and not found phrases
        List<Phrase> found = new ArrayList<Phrase>();
        List<Phrase> notFound = new ArrayList<Phrase>();
        List<Phrase> incorrect = new ArrayList<Phrase>();
        for(Phrase ph : doc.getKeyphrases()) {
            if (kphrases.contains(ph)) found.add(ph);
            else notFound.add(ph);
        }
        for (Phrase ph : kphrases) {
            if (!doc.getKeyphrases().contains(ph)) incorrect.add(ph);
        }
        // print found and not found phrases
        out.println("correctly found :");
        PhraseHelper.printPhraseSet(out, found, 5, true);        
        out.println();      
        out.println("incorrectly found :");
        PhraseHelper.printPhraseSet(out, incorrect, 5, true);        
        out.println();        
        out.println("not found :");
        PhraseHelper.printPhraseSet(out, notFound, 5, true);        
        out.println();                
        
        F1Evaluator eval = new F1Evaluator(extr, IPhraseEquality.PhEquality.SEMEVAL);
        F1Metric result = eval.evaluateResult(kphrases, doc.getKeyphrases());
        out.println(result);
        
        out.close();
    }
    
    // analyze coverage of the document by phrase subsets 
    public void coverage(List<Phrase> phrases, GreedyExtractorConfig c, int setSize) 
                    throws Exception {
        initFolder();
        c.adaptToDocument(doc.getText());
        IRealVector documentVector = c.docVectorizer.vectorize(doc.getText());
        int n = phrases.size();
        generateSubsets(phrases, setSize, 0, -1);
        List<Double> score = new ArrayList<Double>();
        for (List<Phrase> ss : subsets) {
            c.phVectorizer.clear();
            for (Phrase ph : ss) c.phVectorizer.addPhrase(ph);
            IRealVector vec = c.phVectorizer.vector();
            double sc = c.phraseSetQuality.compare(vec, documentVector);
            score.add(sc);
        }        
        
        Utils.sort(subsets, score, true);
        
        PrintStream out = new PrintStream(outFolder+"coverage"+setSize+".txt");           
        for (int i = 0; i < subsets.size(); ++i) {
            List<Phrase> ss = subsets.get(i);
            out.print("score: " + Utils.doubleStr(score.get(i),3) + " ");
            PhraseHelper.printPhraseSet(out, ss, 10, true);            
        }
        out.close();
    }
    
    private void generateSubsets(List<Phrase> ph, int size, int d, int i) {
        if (d == 0) {
            subsets = new ArrayList<List<Phrase>>();
            subset = new ArrayList<Phrase>();
            generateSubsets(ph, size, 1, 0);            
        }        
        else if (d <= size) {            
            for (int j = i; j < ph.size(); ++j) {
                subset.add(ph.get(j));
                generateSubsets(ph, size, d+1, j+1);
                subset.remove(subset.size()-1);
            }
        }
        else {
            subsets.add(new ArrayList<Phrase>(subset));
        }
    }
    
}
