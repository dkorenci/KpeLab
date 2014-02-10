/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.analysis;

import hr.irb.zel.kpelab.experiments.WordSim353Helper;
import hr.irb.zel.kpelab.experiments.WordSim353Helper.WordSimPair;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class WS353CorrelationAnalysis {

    public static void outputSimilarityTable(String outputFile, 
            IWordSimilarityCalculator wsim, boolean stem) 
            throws Exception {
        List<WordSimPair> pairs = WordSim353Helper.getNormalizedDataset(stem);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        int notCovered = 0;
        String SEP = ",";
        // print header line
        writer.write("words"+SEP+"sim"+SEP+"vsim"+"\n") ;
        for (WordSimPair pair : pairs) {
            double vsim; 
            try {
                vsim = wsim.similarity(pair.word1, pair.word2);
            }
            catch (SimilarityCalculationException e) {
                notCovered++;
                continue;
            }
            writer.write(pair.word1+"|"+pair.word2+ SEP +pair.similarity+SEP+vsim+"\n");
        }
        writer.close();
        System.out.println(pairs.size() + "pairs , " + (pairs.size()-notCovered) + " covered") ;
    }
    
}
