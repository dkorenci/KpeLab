/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.ArrayRealVector;
import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Read word vectors from two files, one containing a list of words 
 * and other containing vectors in text format, one vector per line, 
 * each vector corresponding to the word with same line number.
 * Store the data in memory.
 */
public class WordToVectorMemMap extends WordToVectorMapBase implements IWordToVectorMap {
    
    protected List<double[]> vectors;
    
    /** Create word to line map and read vectors into memory. */
    public WordToVectorMemMap(String wordFile, String vectorFile) 
            throws IOException, VectorDataFormatException, ClassNotFoundException {
        super(wordFile, vectorFile);
        createWordToLineMap();
        readVectors();                
    }
    
    public IRealVector getWordVector(String word) {
        Long line = wordToLine.get(word);
        if (line == null) return null;        
        long l = line; // convert to basic type to be able to cast to int
        return new ArrayRealVector(vectors.get((int)l));
    }

    private void readVectors() throws FileNotFoundException, IOException, 
            VectorDataFormatException {
        BufferedReader reader = new BufferedReader(new FileReader(this.vectorFile)); 
        vectors = new ArrayList<double[]>();
        String line; int lineCounter = 0;
        int N = -1; // vector width
        while ((line = reader.readLine()) != null) {
            String [] vecStrings = line.split("\\s");
            if (N == -1) N = vecStrings.length;
            else if (N != vecStrings.length) 
                throw new VectorDataFormatException("Vectors are not of the same width, "
                        + "vector at line " + lineCounter + " has different length.");
            lineCounter = 0;
            double [] vector = new double[N];
            for (int i = 0; i < N; ++i) vector[i] = Double.valueOf(vecStrings[i]);
            vectors.add(vector);
        }
    }

}
