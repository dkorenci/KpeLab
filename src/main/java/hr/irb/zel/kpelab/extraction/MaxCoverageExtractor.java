/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.coverage.phrase.IPhraseCoverage;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.similarity.SimilarityCalculationException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Extracts keyphrases by searching for a set of phrases that maximizes 
 * semantic coverage of the document.
 */
public class MaxCoverageExtractor implements IKpextractor {

    PosRegexPhraseExtractor extractor;    
    IPhraseCoverage phraseCoverage;
    private int phraseSetSize; 
    List<Phrase> solution; // optimal set of phrases
    double solQual; // quality of the optimal solution
    List<Phrase> phrases; // all document phrases
    Random randomGen;
    
    private static final double percIncStop = 0.01;
    private static final int NO_IMPROVE_STEPS = 5;
    private static final int MAX_ITER = 50;
    
    
    public MaxCoverageExtractor(PosRegexPhraseExtractor extr, IPhraseCoverage cov, int K) {
        extractor = extr; phraseCoverage = cov; phraseSetSize = K;
    }
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        phrases = extractor.extractPhrases(doc.getText());
        randomGen = new Random(7786654);
        tabuSearch();
        return solution;
    }

    private void tabuSearch() throws Exception {
        List<Phrase> phraseSet = getRandomPhraseSet(phraseSetSize);
        solution = phraseSet; solQual = calculateSolutionQuality(solution);        
        Queue<Phrase> insertTabu = new LinkedList<Phrase>(); int ITS = 10;
        Queue<Phrase> removeTabu = new LinkedList<Phrase>(); int RTS = 2;
        int iterationCounter = 0, noImprove = 0;        
        //System.out.println(phrases.size() + " , " + phraseSet.size());        
        while (true) {
            //printPhraseSet(phraseSet, 3);
            List<Phrase> optSol = null; // max. quality solution in the neighbourhood                        
            double optQual = Double.MIN_VALUE; 
            int optI = -1, optJ = -1; // phrases that give optimal solution
            List<Phrase> newSol = new ArrayList<Phrase>();
            // search solution neighbourhood
            for (int i = 0; i < phraseSet.size(); ++i) { // phrase to remove
            for (int j = 0; j < phrases.size(); ++j) {                 
                // check tabu conditions
                if (!removeTabu.contains(phraseSet.get(i)) && 
                    !insertTabu.contains(phrases.get(j))) { 
                    // remove phrase i, include phrase j                
                    // construct new solution
                    newSol.clear();
                    for (int k = 0; k < phraseSet.size(); ++k) {
                        if (k != i) newSol.add(phraseSet.get(k));
                        else newSol.add(phrases.get(j));
                    }
                    // calculate quality of the new solution
                    double qual = calculateSolutionQuality(newSol);
                    if (qual > optQual) {
                        optQual = qual; optSol = new ArrayList(newSol);
                        optI = i; optJ = j;
                    }
                }
            }
            }            
            // update tabu            
            if (insertTabu.size() == ITS) insertTabu.remove();
            if (removeTabu.size() == RTS) removeTabu.remove();
            insertTabu.add(phrases.get(optJ));
            removeTabu.add(phraseSet.get(optI));
            //System.out.println(insertTabu.size() + " " + removeTabu.size());
            // update phrase set
            phraseSet = optSol;
            // update global solution
            assert(phraseSet.size() == solution.size());
            if (optQual > solQual) {
                solQual = optQual;
                solution = phraseSet;
                noImprove = 0;
            }            
            else noImprove++;
            iterationCounter++;
            //System.out.println(iterationCounter + " " + solQual);            
            if (iterationCounter >= MAX_ITER) break;
            if (noImprove == NO_IMPROVE_STEPS) break;
        }
    }

    private List<Phrase> getRandomPhraseSet(int size) {
        List<Phrase> ph = new ArrayList<Phrase>();
        List<Phrase> allPhrases = new ArrayList<Phrase>(phrases);        
        for (int i = 0; i < size && i < phrases.size(); ++i) {
            int rndIndex = randomGen.nextInt(allPhrases.size());
            ph.add(allPhrases.get(rndIndex));
            allPhrases.remove(rndIndex);
        }
        return ph;
    }

    private void printPhraseSet(List<Phrase> phrases, int phrasesPerRow) {
        int ppr = 0;
        for (int i = 0; i < phrases.size(); ++i) { 
            Phrase ph = phrases.get(i);
            System.out.print(ph+" ; ");            
            if (++ppr == phrasesPerRow) {
                ppr = 0; 
                if (i == phrases.size()-1) System.out.println("-----"); // last phrase      
                else System.out.println();       
            }
        }       
        if (ppr > 0) System.out.println("-----");
    }
    
    private double calculateSolutionQuality(List<Phrase> sol) throws Exception {
        double qual = 0;
        for (Phrase phDoc : phrases) {
            double maxCov = Double.MIN_VALUE;
            for (Phrase phSol : sol) { 
                double cov; 
                try {
                    cov = phraseCoverage.coverage(phSol, phDoc);
                }
                catch (SimilarityCalculationException ex) { 
                    cov = 0;
                }
                if (cov > maxCov) 
                    maxCov = cov;
            }
            qual += maxCov;
        }
        return qual;
    }

}
