package hr.irb.zel.kpelab.inspector;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/** Output relevant kpe data for a document.  */
public class KpeInspector {

    private GreedyExtractorConfig config;
    private String baseFolder, outFolder;
    private KpeDocument doc;
    private IRealVector docVector;
    
    public KpeInspector(GreedyExtractorConfig c) {
        config = c;
        baseFolder = KpeConfig.getProperty("inspect.output");
    }
        
    public void inspect(KpeDocument d) throws Exception {
        doc = d;
        initFolder();
        phrasesBySimilarity();
    }

    private void initFolder() throws IOException {
        outFolder = baseFolder + doc.getId()+config.getId()+"/";
        File folder = new File(outFolder);
        if (folder.exists()) { // delete files
            for (File f : folder.listFiles()) f.delete();
        }
        else { // create folder
            folder.mkdir();
        }
    }

    // output all candidate phrases, sorted by similarity in descending order
    private void phrasesBySimilarity() throws Exception {
        GreedyExtractor extr = new GreedyExtractor(5, config);
        extr.prepareForExtraction(doc);
        PrintStream out = new PrintStream(outFolder+"phrasesBySim.txt");
        out.println("solution phrases: ");
        PhraseHelper.printPhraseSet(out, doc.getKeyphrases(), 5);
        out.println("phrases by similarity: ");
        extr.printRankedCandidates(out);
        out.close();
    }

    private void aggregateDocumentVector() throws Exception {
        if (docVector != null) 
            docVector = config.docVectorizer.vectorize(doc.getText());        
    }
    
}
