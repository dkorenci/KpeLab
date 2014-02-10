/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSetSimilarity;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class ExperimentsHelper {
    
    private static class Sim<T> implements Comparable<Sim<T>> {
        public T data;
        public double sim;

        // sort descending by similarity
        public int compareTo(Sim<T> o) {
            if (sim > o.sim) return -1;
            else if (sim < o.sim) return 1;
            else return 0;
        }
        
    }    
    
   /** For a set of words, for each word rank other words by similarity. */
    public static void rankWordsBySimilarity(List<String> words, IWordSimilarityCalculator sim) 
            throws Exception {
        final int wordsPerLine = 30;
        for (int i = 0; i < words.size(); ++i) {
            System.out.print(words.get(i)+ " ");
            if (i > 0 && i % wordsPerLine == 0 || i == words.size()-1) 
                System.out.println();
        }
        for (String word : words) {
            List<Sim<String>> sims = new ArrayList<Sim<String>>();
            for (int i = 0; i < words.size(); ++i) {
                Sim<String> wsim = new Sim<String>();
                wsim.sim = sim.similarity(word, words.get(i));                
                wsim.data = words.get(i);                
                sims.add(wsim);
            }
            Collections.sort(sims);
            //Collections.sort(Arrays.asList(sims));
            System.out.println(word + ":");
            int i = 0;
            for (Sim<String> wsim : sims) {                
                System.out.print(wsim.data + ", " + String.format("%.3f", wsim.sim) + " ; ");
                if (i > 0 && i % wordsPerLine == 0 || i == words.size()-1) 
                    System.out.println();
                i++;
            }
        }
    }

   /** For a set of phrases, for each word rank other words by similarity. */
    public static void rankPhrasesBySimilarity(List<Phrase> phrases, IPhraseSimilarityCalculator sim) 
            throws Exception {
        final int phrasesPerLine = 30;
        for (int i = 0; i < phrases.size(); ++i) {
            System.out.print(phrases.get(i)+ " ; ");
            if (i > 0 && i % phrasesPerLine == 0 || i == phrases.size()-1) 
                System.out.println();
        }
        for (Phrase ph : phrases) {
            List<Sim<Phrase>> sims = new ArrayList<Sim<Phrase>>();
            for (int i = 0; i < phrases.size(); ++i) {
                Sim<Phrase> wsim = new Sim<Phrase>();
                try {
                    wsim.sim = sim.similarity(ph, phrases.get(i));                
                }
                catch (SimilarityCalculationException e) {
                    //System.out.println(ph + " , " + phrases.get(i));
                    //e.printStackTrace();
                    wsim.sim = Double.MIN_VALUE;
                }
                wsim.data = phrases.get(i);                
                sims.add(wsim);
            }
            Collections.sort(sims);
            //Collections.sort(Arrays.asList(sims));
            System.out.println(ph + ":");
            int i = 0;
            for (Sim<Phrase> phsim : sims) {                
                System.out.print(phsim.data + ", " + String.format("%.3f", phsim.sim) + " ; ");
                if (i > 0 && i % phrasesPerLine == 0 || i == phrases.size()-1) 
                    System.out.println();
                i++;
            }
        }
    }    
  
    /** Generate N random phrasesets, subsets of phrases of size S, 
     sort by similarity and print top K and bottom K subsets. */
    public static void rankPhrasesetsBySimilarity(List<Phrase> phrases,
            IPhraseSetSimilarity phraseSetSim, int N, int S, int K) {
        List<Sim<List<Phrase>>> simsets = new ArrayList<Sim<List<Phrase>>>();
        Set<String> indSets = new TreeSet<String>(); // set of generated subsets of indexes
        int numPh = phrases.size();
        
        Random randGen = new Random(15678);
        int [] indexes = new int[S];
        for (int i = 0; i < N; ++i) {
            //System.out.print(i + " ");
            // generate S phrases (their indexes), not equal to any
            // previous set and such that similarity is computable
            while (true) {
                // generate S phrases (their indexes)
                for (int j = 0; j < S; ++j) {
                    // generate phrases at position j until it is different
                    // from previously generated phrases
                    while (true) {
                        indexes[j] = randGen.nextInt(numPh);
                        boolean inTheSet = false;
                        for (int k = 0; k < j; ++k) {
                            if (indexes[k] == indexes[j]) {
                                inTheSet = true;
                                break;
                            }
                        }
                        if (inTheSet) continue; // try with another random phrase
                        else break; // proceed with next phrase
                    }
                }
                // check if the index set is already generated
                Arrays.sort(indexes);
                String indexesStr = Integer.toString(indexes[0]);
                for (int j = 1; j < S; ++j) indexesStr = indexesStr + (","+indexes[j]);
                if (indSets.contains(indexesStr)) continue;
                // check if similarity of the phrases indexed by the set is computable
                List<Phrase> indPhrases = new ArrayList<Phrase>();
                for (int j = 0; j < S; ++j) { indPhrases.add(phrases.get(indexes[j])); }
                double sim;
                try {
                    sim = phraseSetSim.similarity(indPhrases);
                }        
                catch (SimilarityCalculationException e) {
                    continue;
                }
                // phrase set is good, add to generated sets and continue with next set
                indSets.add(indexesStr);                
                Sim<List<Phrase>> phSim = new Sim<List<Phrase>>();
                phSim.data = indPhrases;
                phSim.sim = sim;
                simsets.add(phSim);
                break;                
            }
        }
        
        Collections.sort(simsets);        
        System.out.println(K + " most dissimilar subsets: ");
        for (int i = simsets.size()-1; i >= simsets.size()-K && i >= 0; --i) {
            List<Phrase> subset = simsets.get(i).data;
            String str = "";
            for (Phrase ph : subset) str += ph + " ; ";
            str += " sim: " + simsets.get(i).sim;
            System.out.println(str);
        }
        System.out.println(K + " most similar subsets: ");
        for (int i = 0; i < K; ++i) {
            List<Phrase> subset = simsets.get(i).data;
            String str = "";
            for (Phrase ph : subset) str += ph + " ; ";
            str += " sim: " + simsets.get(i).sim;
            System.out.println(str);
        }        
    }
}
   
