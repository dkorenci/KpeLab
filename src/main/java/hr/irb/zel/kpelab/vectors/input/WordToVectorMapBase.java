/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectors.input;

import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import hr.irb.zel.kpelab.config.KpeConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common functionality for {@link WordToVectorDiskMap} and {@link WordToVectorMemMap}.
 */
public class WordToVectorMapBase {
    
    protected String wordFile, vectorFile;
    protected Map<String, Long> wordToLine;
    protected static final String cacheFolder = KpeConfig.getProperty("cache.folder");        
    private final String id;

    public boolean hasWord(String word) {
        return this.wordToLine.containsKey(word);
    }    
    
    public String getId() { return id; }

    public Collection<String> getWords() {
        return Collections.unmodifiableSet(wordToLine.keySet());
    }        
    
    protected WordToVectorMapBase(String wordFile, String vectorFile, String id) 
            throws FileNotFoundException, IOException, ClassNotFoundException, 
            VectorDataFormatException {
        this.wordFile = wordFile; this.vectorFile = vectorFile; this.id = id;
        createWordToLineMap();
    }
    
    // id string that identifies current combination of vector file and word file
    protected String getMappingId() {
        File vec = new File(vectorFile), wrd = new File(wordFile);
        return "vectors_"+vec.getName()+"_words_"+wrd.getName();
    }    
    
    // deserialize word to line map or create from file
    protected final void createWordToLineMap() throws FileNotFoundException, IOException, 
            ClassNotFoundException, VectorDataFormatException {        
        File cacheFile = new File(cacheFolder + "word_to_line_" + getMappingId());
        if (cacheFile.exists()) {
            System.out.print("reading cached wordToLineMap... ");
            ObjectIO ss = new ObjectIO(cacheFile, false);
            wordToLine = (Map<String, Long>)ss.readObject();
            ss.close();            
            System.out.println("done.");
        }
        else {
            System.out.print("reading wordToLineMap... ");
            readWordToLineMap();
            ObjectIO ss = new ObjectIO(cacheFile, true);
            ss.writeObject(wordToLine);
            ss.close();
            System.out.println("done.");
        }
    }
    
    // create word to line map from file
    private void readWordToLineMap() throws FileNotFoundException, IOException, 
            VectorDataFormatException {
        BufferedReader reader = new BufferedReader(new FileReader(this.wordFile)); 
        this.wordToLine = new TreeMap<String, Long>();
        String word; long lineCounter = 0;
        while ((word = reader.readLine()) != null) {
//            if (this.wordToLine.containsKey(word))
//                throw new VectorDataFormatException("duplicate word: " + word +
//                        " at line " + lineCounter);
            if (this.wordToLine.containsKey(word) == false)            
                this.wordToLine.put(word, lineCounter); 
            lineCounter++;
        }
        reader.close();        
    }
    
    // wrapper stream for serialization and deserialization of objects
    protected static class ObjectIO {
        private FSTObjectInput objectReader;
        private FSTObjectOutput objectWriter; 
//        private ObjectInputStream objectReader;
//        private ObjectOutputStream objectWriter; 
        
        
        /** @param serialize If true open serialization stream, else open deserialization stream. */
        public ObjectIO(File file, boolean serialize) throws IOException {            
            if (serialize) objectWriter = new FSTObjectOutput(new FileOutputStream(file));
            else objectReader = new FSTObjectInput(new FileInputStream(file));                              
//            if (serialize) objectWriter = new ObjectOutputStream(new FileOutputStream(file));
//            else objectReader = new ObjectInputStream(new FileInputStream(file));               
        }
        
        public Object readObject() throws ClassNotFoundException, IOException {
            return objectReader.readObject();
        }
        
        public void writeObject(Object o) throws IOException {
            objectWriter.writeObject(o);
        }
        
        public void close() throws IOException {
            if (objectReader != null) objectReader.close();
            if (objectWriter != null) objectWriter.close();
        }
        
    }
}
