package hr.irb.zel.kpelab.df;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.term.TermExtractor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.uima.UIMAException;

/** Class for creating and providing term-document frequency counts. */
public class TermDocumentFrequency {

    TermExtractor extractor;    
    private Map<String, Integer> termDf;
    private int numDocuments;
    
    // folder for saving counters
    private static final String repositoryFolder = KpeConfig.getProperty("cache.folder");     
    
    public TermDocumentFrequency(TermExtractor ext) { extractor = ext; }
    
    // deserialize df data from file
    public TermDocumentFrequency(String fileName) throws IOException, ClassNotFoundException {
        loadDfData(fileName);
    }
    
    // create frequency counts for a document collection and save to file
    public void createTermFrequency(List<KpeDocument> docs, String fileId) throws UIMAException, IOException {
        termDf = new TreeMap<String, Integer>();
        numDocuments = docs.size();
        for (KpeDocument doc : docs) {
            List<String> terms = extractor.extract(doc.getText());
            for (String term : terms) {
                if (termDf.containsKey(term)) {
                    termDf.put(term, termDf.get(term)+1);
                }
                else {
                    termDf.put(term, 1);
                }
            }
        }
        saveDfData(fileId);
    }
    
    public int documentFrequency(String term) { 
        Integer df = termDf.get(term);
        if (df == null) return 0;
        else return df;
    }
    
    public int getNumDocuments() { return numDocuments; }

    // serialize df data to file
    private void saveDfData(String fileName) throws IOException {
        String filePath = repositoryFolder + fileName;
        FileOutputStream file = new FileOutputStream(filePath);
        ObjectOutputStream oos = new ObjectOutputStream(file);
        oos.writeObject(termDf);        
        oos.writeInt(numDocuments);
        oos.close();        
    }

    // deserialize df data from file
    private void loadDfData(String fileName) throws IOException, ClassNotFoundException {
        String filePath = repositoryFolder + fileName;
        FileInputStream file = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(file);
        termDf = (Map<String, Integer>)ois.readObject();
        numDocuments = ois.readInt();
        ois.close();
    }    
    
}
