package hr.irb.zel.kpelab.extraction.tabu;

import hr.irb.zel.kpelab.extraction.IKpextractor;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.coverage.phrase.IPhraseCoverage;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
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
public class KpeTabuSearch implements IKpextractor {
        
    private ISearchPhraseSet searchSet;
    private int phraseSetSize; 
    List<Phrase> solution; // optimal set of phrases
    double solQual; // quality of the optimal solution
    List<Phrase> phrases; // all document phrases
    Random randomGen;
    
    private static final double percIncStop = 0.01;
    private static final int NO_IMPROVE_STEPS = 3;
    private static final int MAX_ITER = 50;
    
    // tabu size parameters
    private static final int prefITS = 10, prefRTS = 2;
    private int ITS, RTS;    
    
    public KpeTabuSearch(ISearchPhraseSet sc, int K) {
        searchSet = sc; phraseSetSize = K;
    }
    
    public String getId() { return "tabu"; }
    
    public List<Phrase> extract(KpeDocument doc) throws Exception {
        searchSet.setDocument(doc);        
        phrases = searchSet.getDocumentPhrases();     
//        System.out.println("all phrases: ");
//        printPhraseSet(phrases, 7);
        randomGen = new Random(7786654);
        if (phraseSetSize >= phrases.size()) {
            return phrases;
        }
        else {            
            adjustTabuParameters();
            tabuSearch();
            return solution;
        }
    }

    private void adjustTabuParameters() {
        // number of phrases in the phrase set + insert tabu size
        // must be less than number of all phrases        
        int diff = phrases.size() - phraseSetSize;        
        int maxITS = diff/2;
        if (maxITS >= prefITS) ITS = prefITS;
        else ITS = maxITS;
        
        int maxRTS = phraseSetSize/4;
        if (maxRTS >= prefRTS) RTS = prefRTS;
        else RTS = maxRTS;               
    }
    
    private void tabuSearch() throws Exception {                
        solution = getRandomPhraseSet(phraseSetSize);
        searchSet.setPhraseSet(solution);        
        solQual = searchSet.calculateQuality();
        Queue<Phrase> insertTabu = new LinkedList<Phrase>(); 
        Queue<Phrase> removeTabu = new LinkedList<Phrase>();
        int iterationCounter = 0, noImprove = 0;        
        System.out.println("starting tabu search");
        System.out.println(phrases.size() + " , " + phraseSetSize);        
        while (true) {
            //printPhraseSet(solution, 5);                                
            double optQual = Double.MIN_VALUE; 
            int optI = -1, optJ = -1; // phrases that give optimal solution            
            // search solution neighbourhood
            for (int i = 0; i < searchSet.numPhrases(); ++i) { // phrase to remove
            for (int j = 0; j < phrases.size(); ++j) {                 
                // check tabu conditions
                if (!removeTabu.contains(searchSet.getPhrase(i)) && 
                    !insertTabu.contains(phrases.get(j)) &&
                    !searchSet.getPhrase(i).equals(phrases.get(j)) &&
                    !searchSet.containsPhrase(phrases.get(j)) ) 
                { 
                    // remove phrase i, include phrase j                
                    // construct new solution
                    Phrase ph = searchSet.getPhrase(i);
                    searchSet.replacePhrase(i, phrases.get(j));                    
                    // calculate quality of the new solution
                    double qual = searchSet.calculateQuality();
                    if (qual > optQual) {
                        optQual = qual; 
                        optI = i; optJ = j;
                    }                                        
                    searchSet.replacePhrase(i, ph); // ! alternativa je clone()
                }
            }
            }            
            // update tabu            
            if (ITS > 0) {
                if (insertTabu.size() == ITS) insertTabu.remove();
                insertTabu.add(searchSet.getPhrase(optI) );
            }
            if (RTS > 0) {
                if (removeTabu.size() == RTS) removeTabu.remove();            
                removeTabu.add(phrases.get(optJ));         
            }
            // update search set to optimal solution
            searchSet.replacePhrase(optI, phrases.get(optJ));
            // update global solution        
            //printDebugData(optQual, insertTabu, removeTabu);
            System.out.println(optQual);
            if (optQual > solQual) {
                solQual = optQual;
                solution = searchSet.getPhrases();
                noImprove = 0;
            }            
            else noImprove++;
            iterationCounter++;            
            if (iterationCounter >= MAX_ITER) break;
            if (noImprove == NO_IMPROVE_STEPS) break;
        }
    }

    private void printDebugData(double optQual, Queue<Phrase> insertTabu, Queue<Phrase> removeTabu) {
        System.out.println("insert tabu: ");
        PhraseHelper.printPhraseSet((List<Phrase>)insertTabu, 7, false);
        System.out.println("remove tabu: ");
        PhraseHelper.printPhraseSet((List<Phrase>)removeTabu, 7, false);        
        
        System.out.println("optimal solution for this loop: ");
        PhraseHelper.printPhraseSet(searchSet.getPhrases(), 7, false);
        System.out.println("optimal value: " + optQual);
        searchSet.printDebugData();        
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

}
