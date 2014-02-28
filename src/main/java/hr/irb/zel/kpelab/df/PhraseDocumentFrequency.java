package hr.irb.zel.kpelab.df;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.uima.UIMAException;

/**
 * Counts the number of phrase occurences in a set of documents.
 */
public class PhraseDocumentFrequency {

    private Collection<KpeDocument> documents;
    private IPhraseExtractor phraseExtractor;
    private Map<Phrase, Integer> phraseCount;
    private int numDocuments;
    
    // folder for saving counters
    private static final String repositoryFolder = KpeConfig.getProperty("cache.folder"); 
    
    /** Create counter by extracting phrases from collection and counting occurrences. */
    public PhraseDocumentFrequency(Collection<KpeDocument> docs, IPhraseExtractor phExtr) throws Exception {
        documents = docs; numDocuments = docs.size();
        phraseExtractor = phExtr;
        createPhraseCount();                
    }
    
    /** Create counter by reading phrase -> count map from file. */
    public PhraseDocumentFrequency(String counterId) throws Exception {
        String fileName = repositoryFolder + counterId;
        FileInputStream file = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(file);
        phraseCount = (Map<Phrase, Integer>)ois.readObject();
        numDocuments = ois.readInt();
        ois.close();
    }
    
    public int countOccurences(Phrase ph) {
        Integer count = phraseCount.get(ph);
        if (count == null) return 0;
        else return count;
    }

    /** Persist phrase->count mapping to counterId file. */
    public void saveToFile(String counterId) throws FileNotFoundException, IOException {
        String fileName = repositoryFolder + counterId;
        FileOutputStream file = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(file);
        oos.writeObject(phraseCount);        
        oos.writeInt(numDocuments);
        oos.close();
    }
    
    private void createPhraseCount() throws Exception {
        phraseCount = new TreeMap<Phrase, Integer>();        
        System.out.println("all docs: " + documents.size());
        int cnt = 0;
        for (KpeDocument doc : documents) {                        
            List<Phrase> phrases = phraseExtractor.extractPhrases(doc.getText());
            for (Phrase ph : phrases) {
                Integer count = phraseCount.get(ph);
                if (count == null) phraseCount.put(ph, 1);
                else phraseCount.put(ph, count + 1);                
            }
            cnt++;
            if (cnt % 100 == 0) {
                System.out.println("num docs: " + cnt);
                System.out.println("number of phrases: " + phraseCount.size());
            }
        }
    }

    public Integer getNumDocuments() {
        return numDocuments;
    }
        
}
