/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.corpus.hulth;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class CorpusHulth {
    public static String corpusLocation = "/data/datasets/kpe/Hulth2003/";
    
    /** Get all documents form one of the 'Test','Training' or 'Validation' folders.       
     * @param readPhrases weather to read solution keyphrases for a document or not
     */
    public static List<KpeDocument> getDocuments(String folder, 
            boolean readPhrases, CanonicForm canonic) 
            throws Exception {
        List<File> files = getFilesInFolder(corpusLocation + folder);
        DocumentReaderHulth reader = new DocumentReaderHulth(readPhrases, canonic);
        List<KpeDocument> docs = new ArrayList<KpeDocument>();
        for (File file : files) {
            docs.add(reader.readDocument(file));            
        }
        return docs;
    }
    
    /** Get all documents in the corpus. */
    public static List<KpeDocument> getAllDocuments(boolean readPhrases, 
            CanonicForm canonic) throws Exception {
         List<KpeDocument> docs = new ArrayList<KpeDocument>();
         docs.addAll(getDocuments("Test", readPhrases, canonic));
         docs.addAll(getDocuments("Training", readPhrases, canonic));
         docs.addAll(getDocuments("Validation", readPhrases, canonic));
         return docs;
    }
    
    private static List<File> getFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        List<File> files = new ArrayList<File>();
        // filter abstract files from folder
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".abstr"))
                files.add(file);
        }        
        return files;
    }
           
}
