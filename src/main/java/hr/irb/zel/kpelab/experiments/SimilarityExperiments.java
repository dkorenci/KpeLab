/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.experiments;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.CorpusHulth;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.coverage.phrase.MaxWordSimilarityCoverage;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PhraseHelper.TokenCanonic;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSetSimilarity;
import hr.irb.zel.kpelab.similarity.phrase.IPhraseSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.phrase.PhraseAvgSimilairty;
import hr.irb.zel.kpelab.similarity.phrase.PhraseSetAvgSimilarity;
import hr.irb.zel.kpelab.similarity.phrase.PhraseSumSimilarity;
import hr.irb.zel.kpelab.similarity.word.IWordSimilarityCalculator;
import hr.irb.zel.kpelab.similarity.word.VectorWordSimilarity;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.WordToVectorDiskMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity.SimilarityMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.uima.UIMAException;

public class SimilarityExperiments {

    private static String [] wordSet1 = {"idea", "philosophy", "abstract", "truth", "god", 
                "car", "boat", "wheel", "windshield", "bus", 
                "computer", "monitor", "screen", "program", "algorithm"};    
    
    private static List<String> getWordsStems(String [] words) throws UIMAException {
        List<String> result = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; ++i)  {
            builder.append(words[i]);
            if (i < words.length - 1) builder.append(" ");
        }
        List<TokenCanonic> canonic = 
                PhraseHelper.getCanonicForms(builder.toString(), CanonicForm.STEM);
        for (TokenCanonic tcan : canonic) result.add(tcan.canonic);
        return result;
    }
    
    public static void experiment1() throws Exception {   
        IWordSimilarityCalculator sim = new VectorWordSimilarity(
                WordVectorMapFactory.getCwEmbeddings(), 
                new VectorSimilarity(SimilarityMeasure.L2_NEGATE));
        ExperimentsHelper.rankWordsBySimilarity(Arrays.asList(wordSet1), sim);
    }
    
    public static void experiment2() throws Exception {          
        IWordSimilarityCalculator sim = new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));        
        ExperimentsHelper.rankWordsBySimilarity(Arrays.asList(wordSet1), sim);        
    }    

    // calculate mutual similarity of a list of stemmed words using ESA
    public static void wordStemESASim() throws Exception {          
        IWordSimilarityCalculator sim = new VectorWordSimilarity(
                WordVectorMapFactory.getESAVectors(),
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));        
        ExperimentsHelper.rankWordsBySimilarity(getWordsStems(wordSet1), sim);        
    }     
    
    // calculate spearman correlation on ws353 dataset
    // using negated euclid distance and CWE vectors
    public static void expWS353CwEmbeddings() throws Exception {    
        IWordSimilarityCalculator sim = new VectorWordSimilarity(
                WordVectorMapFactory.getCwEmbeddings(), 
                new VectorSimilarity(SimilarityMeasure.L2_NEGATE));
        WordSim353Helper.WordSimResult res = WordSim353Helper.testSpearmanCorrelation(sim, false);
        System.out.println("Spearman correlation: " + res.spearman);
        System.out.println("Pairs included : " + res.pairsIncluded);
    }
    
    // calculate spearman correlation on ws353 dataset
    // using coine measure and LSI vectors
    public static void expWS353LSI() throws Exception {
        IWordSimilarityCalculator sim =  new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));        
        WordSim353Helper.WordSimResult res = WordSim353Helper.testSpearmanCorrelation(sim, false);
        System.out.println("Spearman correlation: " + res.spearman);
        System.out.println("Pairs included : " + res.pairsIncluded);
    }      
    
    // calculate spearman correlation on ws353 dataset
    // using coine measure and ESA vectors
    public static void expWS353ESA() throws Exception {
        IWordSimilarityCalculator sim =  new VectorWordSimilarity(
                WordVectorMapFactory.getESAVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE));        
        WordSim353Helper.WordSimResult res = WordSim353Helper.testSpearmanCorrelation(sim, true);
        System.out.println("Spearman correlation: " + res.spearman);
        System.out.println("Pairs included : " + res.pairsIncluded);
    }      
    
    // calculate spearman correlation on ws353 dataset
    // using coine measure and ESA vectors
    public static void expWS353ESA01() throws Exception {
        IWordSimilarityCalculator sim =  new VectorWordSimilarity(
                WordVectorMapFactory.getESA01Vectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));        
        WordSim353Helper.WordSimResult res = WordSim353Helper.testSpearmanCorrelation(sim, true);
        System.out.println("Spearman correlation: " + res.spearman);
        System.out.println("Pairs included : " + res.pairsIncluded);
    }         
    
    // calculate all pairwise phrases similarity, sort for each phrase
    // use average word-word LSI-vector cosine similarity 
    public static void phraseSimExp1() throws Exception {
        // construct word and phrase similairty calculators
        IWordSimilarityCalculator wordSim =  new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));         
        IPhraseSimilarityCalculator phSim = new PhraseAvgSimilairty(wordSim);
        // get phrases from a document
        KpeDocument doc = new DocumentReaderHulth(false, null).readDocument("Training/639.abstr");
        IPhraseExtractor phExtr = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        List<Phrase> phrases = phExtr.extractPhrases(doc.getText());
        // calculate similarities
        ExperimentsHelper.rankPhrasesBySimilarity(phrases, phSim);
    }

    // calculate all pairwise phrases similarity, sort for each phrase    
    // use agreggated word LSI-vectors cosine similarity 
    public static void phraseSimExp2() throws Exception {
        // construct phrase similairty calculators
        IVectorComparison vectorSim = new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF);
        IPhraseSimilarityCalculator phSim = new PhraseSumSimilarity(
                WordVectorMapFactory.getLSIVectors(), vectorSim);
        // get phrases from a document
        KpeDocument doc = 
                new DocumentReaderHulth(false, null).readDocument("Training/639.abstr");
        IPhraseExtractor phExtr = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        List<Phrase> phrases = phExtr.extractPhrases(doc.getText());
        // calculate similarities
        ExperimentsHelper.rankPhrasesBySimilarity(phrases, phSim);
    }    

    // for each phrase sort other phrases by coverage
    // use average word-word LSI-vector cosine similarity 
    public static void phraseSimExpCoverage() throws Exception {
        // construct word and phrase similairty calculators
        IWordSimilarityCalculator wordSim =  new VectorWordSimilarity(
                 WordVectorMapFactory.getLSIVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));         
        IPhraseSimilarityCalculator phSim = new MaxWordSimilarityCoverage(wordSim, false);
        // get phrases from a document
        KpeDocument doc = new DocumentReaderHulth(false, null).readDocument("Training/639.abstr");
        IPhraseExtractor phExtr = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        List<Phrase> phrases = phExtr.extractPhrases(doc.getText());
        // calculate similarities
        ExperimentsHelper.rankPhrasesBySimilarity(phrases, phSim);
    }    
    
    public static void randomPhrasetsExperiment() throws Exception {
        // construct word and phrase similairty calculators
        IWordSimilarityCalculator wordSim =  new VectorWordSimilarity(
                WordVectorMapFactory.getLSIVectors(), 
                new VectorSimilarity(SimilarityMeasure.COSINE_CUTOFF));         
        IPhraseSimilarityCalculator phSim = new PhraseAvgSimilairty(wordSim);        
        IPhraseSetSimilarity phSetSim = new PhraseSetAvgSimilarity(phSim);
        // get phrases from a document
        KpeDocument doc = new DocumentReaderHulth(false, null).readDocument("Training/639.abstr");
        IPhraseExtractor phExtr = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
        List<Phrase> phrases = phExtr.extractPhrases(doc.getText());      
        // generate, sort and print subsets
        ExperimentsHelper.rankPhrasesetsBySimilarity(phrases, phSetSim, 10000, 10, 20);
    }
    
}
 