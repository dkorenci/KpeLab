/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.uima.UIMAException;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

/**
 * Experiments on the Word Similarity 353 dataset.
 */
public class WordSim353Helper {

    private static String datasetFile = KpeConfig.getProperty("dataset.ws353");
    private static List<WordSimPair> dataset;        
    private static Rengine rengine;
    
    public static class WordSimPair {
        
        public WordSimPair(String w1, String w2, double sim) {
            word1 = w1; word2 = w2; similarity = sim;
        }
        
        public String word1, word2;
        public double similarity;
    }
    
    public static class WordSimResult {
        public WordSimResult(double s, int pi) { spearman=s; pairsIncluded=pi; }
        
        public double spearman;
        public int pairsIncluded;
    }
    
    /** Calculate Spearman correlation between dataset pairs ordered by WordSim
     and pairs ordered by dataset (human assigned) similarity. */
    public static WordSimResult testSpearmanCorrelation(IWordSimilarityCalculator wordSim, boolean stem) throws Exception {
        initRengine();        
        List<WordSimPair> data = getDataset();
        double [] datasetSim = new double[data.size()];
        double [] calculatedSim = new double[data.size()];
        
        int cnt = 0; boolean skip;
        for (int i = 0; i < data.size(); ++i) {
            skip = false;
            WordSimPair wsim = data.get(i);
            wsim.word1 = wsim.word1.toLowerCase();
            wsim.word2 = wsim.word2.toLowerCase();
            if (stem) {
                wsim.word1 = PhraseHelper.stemWord(wsim.word1);
                wsim.word2 = PhraseHelper.stemWord(wsim.word2);
            }
            double sim = 0;
            try {
                sim = wordSim.similarity(wsim.word1, wsim.word2);
            }
            catch(SimilarityCalculationException ex) {
                ex.printStackTrace();                
                skip = true;
            }
            if (!skip) {
                datasetSim[cnt] = wsim.similarity;
                calculatedSim[cnt++] = sim;
            }
        }
        
        rengine.assign("x", datasetSim);
        rengine.assign("y", calculatedSim);
        REXP result = rengine.eval("(cor(x,y,method=\"spearman\"))");
        //REXP result = rengine.eval("(cor(x,y,method=\"pearson\"))");
        closeREngine();
                
        return new WordSimResult(result.asDouble(), cnt);
    }
    
    /** Initialize R. */
    private static void initRengine() throws Exception {
        if (rengine != null) return;
        rengine = new Rengine (new String [] {"--vanilla"}, false, null);  
        if (!rengine.waitForR()) {
            throw new Exception("R engine did not initialize");
        }
    }
    
    /** Close R. */
    private static void closeREngine() {
        rengine.end();
        rengine = null;        
    }
    
    public static List<WordSimPair> getDataset() throws FileNotFoundException, IOException {
        if (dataset == null) dataset = readDataset(true);
        return dataset;
    }

    public static List<WordSimPair> getNormalizedDataset(boolean stem) 
            throws FileNotFoundException, IOException, UIMAException {
        List<WordSimPair> pairs = new ArrayList<WordSimPair>(getDataset());
        for (WordSimPair wsim : pairs) {        
            wsim.word1 = wsim.word1.toLowerCase();
            wsim.word2 = wsim.word2.toLowerCase();
            if (stem) {
                wsim.word1 = PhraseHelper.stemWord(wsim.word1);
                wsim.word2 = PhraseHelper.stemWord(wsim.word2);                    
            }
        }
        return pairs;
    }

    
    
    private static List<WordSimPair> readDataset(boolean removeProperNouns) 
            throws FileNotFoundException, IOException {
        // list of proper nouns occuring in the dataset
        String [] propNouns = {"Jerusalem", "Israel", "Palestinian","Maradona", 
        "Arafat", "Jackson", "Japanese", "American", "Harvard", "Yale", 
        "Mexico", "Brazil", "Mars", "OPEC", "FBI", "Wednesday", "Freud"};
        Set<String> propNounsSet = new TreeSet<String>(Arrays.asList(propNouns));
        List<WordSimPair> data = new ArrayList<WordSimPair>();
        BufferedReader reader = new BufferedReader(new FileReader(datasetFile));
        String line; 
        reader.readLine(); // skip header line
        while ((line = reader.readLine()) != null) {
            String [] tokens = line.split(",");
            WordSimPair wsp;
            boolean add = true;
            if (removeProperNouns && 
               (propNounsSet.contains(tokens[0]) || propNounsSet.contains(tokens[1])) ){                
                add = false;    
            }            
            if (add) {
                wsp = new WordSimPair(tokens[0], tokens[1], Double.valueOf(tokens[2]));            
                data.add(wsp);
            }
        }
        return data;
    }
    
}
