/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.ArrayRealVector;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.SparseRealVector;
import static hr.irb.zel.kpelab.vectors.input.WordToVectorMapBase.cacheFolder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;


/**
 * Read word vectors from two files, one containing a list of words 
 * and other containing vectors in text format, one vector per line, 
 * each vector corresponding to the word with same line number.
 * Read the data from vector file on access.
 */
public class WordToVectorDiskMap extends WordToVectorMapBase implements IWordToVectorMap {

    // maps line ordinal to index of the staring byte of the line
    private Map<Long, Long> lineToLocation;    
    private FileChannel vectors;
    private ByteBuffer lineBuffer;
    private int lineLength = -1;
    private int vectorDimension = -1;
    
    private WordToVectorMapCache cache;
    private boolean cached;
    private boolean sparseVectors;
    private static final int cacheCapacity = 1000;
    
    /** Create word to line map and read vectors into memory. */
    public WordToVectorDiskMap(String wordFile, String vectorFile, String id, boolean cch, boolean sparse) 
            throws FileNotFoundException, VectorDataFormatException, IOException, 
            ClassNotFoundException {
        super(wordFile, vectorFile, id);
        cached = cch;
        sparseVectors = sparse;
        if (cached) createCache();          
    }    
    
    public IRealVector getWordVector(String word) throws IOException, 
            VectorDataFormatException, FileNotFoundException, ClassNotFoundException {
        if (cached) {
            if (cache.hasWord(word)) return cache.getWordVector(word);
            else {                
                IRealVector vector = readWordVectorFromFile(word);
                cache.addWordVectorPair(word, vector);                
                return vector;
            }
        }
        else return readWordVectorFromFile(word);        
    }
      
    public void saveCache() throws FileNotFoundException, IOException {
        if (!cached) return;
        System.out.println("saving word to vector map cache: " + getCacheFileName());
        serializeCacheToFile(new File(getCacheFileName()));
    }    
    
    // initialize word to line mapping, line index and vectors file, if necessary
    private void createDataStructures() throws FileNotFoundException, 
            IOException, VectorDataFormatException, ClassNotFoundException {        
        if (wordToLine == null) createWordToLineMap();
        if (lineToLocation == null) createLineIndex(); 
        if (vectors == null) { // open vectors file for reading
            vectors = FileChannel.open(Paths.get(this.vectorFile), StandardOpenOption.READ);   
        }        
    }
    
    public IRealVector readWordVectorFromFile(String word) 
            throws IOException, VectorDataFormatException, FileNotFoundException, 
            IOException, ClassNotFoundException {
        createDataStructures();        
        // fetch vectors line byte position
        Long lineIndex = wordToLine.get(word);
        if (lineIndex == null) return null;
        long bytePos = lineToLocation.get(lineIndex);
        // read line                
        if (lineBuffer == null) lineBuffer = ByteBuffer.allocate(lineLength);
        else lineBuffer.clear();                    
        vectors.position(bytePos);
        vectors.read(lineBuffer);
        lineBuffer.position(0); // reset (reading) position to start of buffer       
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(lineBuffer);
        // read line as string
        String line = charBuffer.toString();
        // remove newline character
        line = line.substring(0, line.indexOf('\n'));
        try {
            if (!sparseVectors) return createDenseVectorFromString(line);        
            else return createSparseVectorFromString(line);
        }
        catch (VectorDataFormatException e) {
            // rethrow with line index information added
            throw new VectorDataFormatException(
                    e.getMessage() + " , line index = " + lineIndex);
        }
    }
    
    /** Create dense vector from a string of whitespace separated numbers. */
    private ArrayRealVector createDenseVectorFromString(String str) 
            throws VectorDataFormatException {       
        String [] tokens = str.split("\\s");
        // check that this vector size matches previous vectors
        if (vectorDimension == -1) vectorDimension = tokens.length;
        else if (vectorDimension != tokens.length) {
            throw new VectorDataFormatException("line is not of same length as previous lines");
        }
        // create vector
        double [] vector = new double[lineLength];
        for (int i = 0; i < tokens.length; ++i) vector[i] = Double.valueOf(tokens[i]);        
        return new ArrayRealVector(vector);        
    }
    
    /** Create sparse vectors from a list of (dimensionIndex, value) pairs.
     * Both pair members and pairs should be whitespace separated. */
    private SparseRealVector createSparseVectorFromString(String str) throws VectorDataFormatException {
        String [] tokens = str.split("\\s");
        if (tokens.length % 2 != 0) {
            throw new VectorDataFormatException("number of tokens is not even");
        }
        SparseRealVector vector = new SparseRealVector();
        for (int i = 0; i < tokens.length; i += 2) {
            int coordinate = Integer.valueOf(tokens[i]);
            double value = Double.valueOf(tokens[i+1]);
            vector.setElement(coordinate, value);
        }
        return vector;
    }
    
    // build map of line ordinals to byte at start of the line, or 
    // deserialize map from file if the file exists
    private void createLineIndex() throws FileNotFoundException, 
            IOException, ClassNotFoundException {        
        File vf = new File(this.vectorFile);
        String vfname = vf.getName();
        File cachedIndex = new File(cacheFolder + "line_index_" + getMappingId());
        if (cachedIndex.exists()) {
            System.out.println("reading lineIndex from cache");
            deserializeLineLocations(cachedIndex);
            return;
        }
        // build index
        System.out.println("reading lineIndex");
        int eol = 10, input, buffSize = 300000000;
        long lineCounter = 0, byteCounter = 0;
        long lastLineStart = 0;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(vf), buffSize);
        lineToLocation = new HashMap<Long, Long>(); //new TreeMap<Long, Long>();
        lineToLocation.put(0l, 0l);                
        while ((input = is.read()) != -1) {
            if (input == eol) {
                lineCounter++;
                lineToLocation.put(lineCounter, byteCounter+1);
                int currLineLength = (int) (byteCounter - lastLineStart + 1);
                if (currLineLength > lineLength) lineLength = currLineLength;       
                lastLineStart = byteCounter + 1;
            }
            byteCounter++;
        }
        is.close();        
        serializeLineLocations(cachedIndex);
    }    

    private void serializeLineLocations(File file) throws IOException {
        ObjectIO ss = new ObjectIO(file, true);
        ss.writeObject(lineToLocation);
        ss.writeObject(lineLength);        
        ss.close();         
    }
    
    private void deserializeLineLocations(File file) throws IOException, 
            ClassNotFoundException {
        ObjectIO ss = new ObjectIO(file, false);
        lineToLocation = (Map<Long, Long>)ss.readObject();
        lineLength = (Integer)ss.readObject();
        ss.close();        
    }

    private void createCache() throws FileNotFoundException, IOException, 
            ClassNotFoundException {
        File cacheFile = new File(getCacheFileName());
        if (cacheFile.exists()) {
            deserializeCacheFromFile(cacheFile);
        }
        else { cache = new WordToVectorMapCache(cacheCapacity); }               
    }    
    
    private String getCacheFileName() {        
        return cacheFolder + "cache_map_" + getMappingId();
    }

    private void deserializeCacheFromFile(File file) throws FileNotFoundException, 
            IOException, ClassNotFoundException {
        ObjectIO ss = new ObjectIO(file, false);
        cache = (WordToVectorMapCache)ss.readObject();        
        ss.close();         
    }
   
    private void serializeCacheToFile(File file) throws IOException {        
        if (file.exists()) file.delete();
        ObjectIO ss = new ObjectIO(file, true);
        ss.writeObject(cache);
        ss.close();         
    }    
    
}
