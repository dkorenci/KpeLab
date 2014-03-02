/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.vectorcoverage.CoverageMeasures;
import hr.irb.zel.kpelab.vectorcoverage.QhullCmdCoverage;
import hr.irb.zel.kpelab.vectors.ArrayRealVector;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.WordToVectorDiskMap;
import hr.irb.zel.kpelab.vectors.input.WordToVectorMemMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CoverageExperiments {

    public static void preliminaryTests() throws Exception {
//        WordToVectorMemMap wvf = new WordToVectorMemMap(
//                "/data/datasets/word_vectors/senna3.0_embeddings/words.lst", 
//                "/data/datasets/word_vectors/senna3.0_embeddings/embeddings.txt");
        WordToVectorDiskMap wvf = new WordToVectorDiskMap(
                "/data/datasets/word_vectors/wiki_lsi/wiki-words.txt", 
                "/data/datasets/word_vectors/wiki_lsi/wiki-matrix.txt", "lsi", true, false);        
        List<List<IRealVector>> sets = new ArrayList<List<IRealVector>>();
        IRealVector zero = new ArrayRealVector(50);
        
        
        List<IRealVector> vectors = new ArrayList<IRealVector>();
        vectors.add(wvf.getWordVector("concept"));
        vectors.add(wvf.getWordVector("idea"));
        vectors.add(wvf.getWordVector("philosophy"));
        vectors.add(zero);
        sets.add(vectors);
        
        vectors = new ArrayList<IRealVector>();
        vectors.add(wvf.getWordVector("truth"));
        vectors.add(wvf.getWordVector("idea"));
        vectors.add(wvf.getWordVector("philosophy"));
        vectors.add(zero);
        sets.add(vectors);
        
        vectors = new ArrayList<IRealVector>();
        vectors.add(wvf.getWordVector("tree"));
        vectors.add(wvf.getWordVector("idea"));
        vectors.add(wvf.getWordVector("dog"));
        vectors.add(zero);
        sets.add(vectors);        
        
        vectors = new ArrayList<IRealVector>();
        vectors.add(wvf.getWordVector("cat"));
        vectors.add(wvf.getWordVector("horse"));
        vectors.add(wvf.getWordVector("dog"));
        vectors.add(zero);
        sets.add(vectors);                
        
        int cnt = 1;
        for (List<IRealVector> vecs : sets) {        
            QhullCmdCoverage coverage = new QhullCmdCoverage();
            CoverageMeasures m = coverage.calculateCoverage(vecs);
            System.out.println("set " + cnt++ + ": " + m);
        }
    }   
    
}
