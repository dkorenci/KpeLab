package hr.irb.zel.kpelab.inspector;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.evaluation.F1Evaluator;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.vectors.IRealVector;
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
        extr.prepareForExtraction(doc);
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
        List<Phrase> kphrases = extr.extract(doc);
        PrintStream out = new PrintStream(outFolder+"keyphrases_"+extr.getId()+".txt");
        
        out.println("correct phrases:");
        PhraseHelper.printPhraseSet(out, doc.getKeyphrases(), 5, true);
        out.println();
        
        out.println("solution phrases:");
        PhraseHelper.printPhraseSet(out, kphrases, 5, true);        
        out.println();
        
        F1Evaluator eval = new F1Evaluator(extr, IPhraseEquality.PhEquality.SEMEVAL);
        F1Metric result = eval.evaluateResult(kphrases, doc.getKeyphrases());
        out.println(result);
        
        out.close();
    }
    
}
