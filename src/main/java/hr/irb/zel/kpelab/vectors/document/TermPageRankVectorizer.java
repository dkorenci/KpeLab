package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.df.TermDocumentFrequency;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.term.WeightedTerm;
import hr.irb.zel.kpelab.term.weighting.ITermWeight;
import hr.irb.zel.kpelab.term.weighting.TfIdfTermWeight;
import hr.irb.zel.kpelab.term.weighting.WeightedList;
import hr.irb.zel.kpelab.util.REngineManager;
import hr.irb.zel.kpelab.util.Utils;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.util.VectorAggregator.Method;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

/** Calculate page rank of document terms. */
public class TermPageRankVectorizer implements IDocumentVectorizer {
            
    // word vectors for document vectorization
    private IWordToVectorMap wordToVector; 
    // word vectors for similarity calculation
    private IWordToVectorMap wordToVectorSim;
    private TermDocumentFrequency tdf;
    private TermExtractor termExtr;
    private CanonicForm cform;
    private List<String> terms;
    private List<WeightedTerm> fTerms;
    private List<WeightedTerm> tfidfTerms;
    private List<WeightedTerm> prTerms;
    private double [] pageRank;
    private int N;
    private String text;
    private double [][] simMatrix;
    private IVectorComparison vectorSim;
    private VectorAggregator agg;
    private Method aggMethod;
    private double dampingFactor;
    private SimMod simMod;
    
    // modification of similarity
    public enum SimMod { ZERO_ONE, EXP, NONE, SQRT }
        
    public TermPageRankVectorizer(IWordToVectorMap wvm, IWordToVectorMap wvmSim, 
            IVectorComparison vsim, CanonicForm cf, TermDocumentFrequency df, 
            Method aggMeth, double d, SimMod sm) {
        wordToVector = wvm; wordToVectorSim = wvmSim; vectorSim = vsim;
        tdf = df; cform = cf;
        agg = new VectorAggregator(wordToVector);
        aggMethod = aggMeth;
        dampingFactor = d;
        simMod = sm;
    }   

    private void initTermExtractor() throws UIMAException {
        if (termExtr == null) {
        termExtr = new TermExtractor(
                new PosExtractorConfig(PosExtractorConfig.Components.OPEN_NLP, cform));               
        }
    }
    
    public void setVectors(IWordToVectorMap wvmap) {
        wordToVector = wvmap;
        agg = new VectorAggregator(wordToVector);
    }    
       
    public IRealVector vectorize(String txt) throws Exception {
        text = txt;       
        process();
        return constructVector();
    }
    
    public void print(String txt) throws Exception {
        text = txt;        
        process();
        printRanks();        
    }

    public List<WeightedTerm> getRanks(String txt) throws Exception {
        text = txt;        
        process();
        return prTerms;
    }    
    
    // do every operation up to and including pagerank
    private void process() throws Exception {
        initTermExtractor();
        createTermList();
        createAdjMatrix();
        if (tdf != null) calculateTfidf();
        calculatePageRank();        
    }

    
    private IRealVector constructVector() throws Exception {
        ITermWeight pagerw = new WeightedList(prTerms);
        IRealVector vec = agg.aggregateWeighted(terms, pagerw, aggMethod);
        System.out.println(vec);
        return vec;
    }
    
    private void printRanks() {        
        Collections.sort(fTerms);
        if (tdf != null) Collections.sort(tfidfTerms);
        Collections.sort(prTerms);        
        for (int i = 0; i < N; ++i) {
//            System.out.print(Utils.fixw(fTerms.get(i).term, 15) +  
//                    Utils.fixw(Utils.doubleStr(fTerms.get(i).weight),10));
//            if (tdf != null) {
//            System.out.print(Utils.fixw(tfidfTerms.get(i).term, 15) +  
//                    Utils.fixw(Utils.doubleStr(tfidfTerms.get(i).weight),10));
//            }
            System.out.print(Utils.fixw(prTerms.get(i).term, 15) +  
                    Utils.fixw(Utils.doubleStr(prTerms.get(i).weight, 10),15)); 
            System.out.println();
        }
    }
    
    // create list of terms that exist in wordToVectorSim map, 
    // together with their frequency weight
    private void createTermList() throws UIMAException {
        List<WeightedTerm> allTerms = termExtr.extractWeighted(text);
        fTerms = new ArrayList<WeightedTerm>(allTerms.size());        
        terms = new ArrayList<String>(allTerms.size());
        for (WeightedTerm t : allTerms) {
            if (wordToVectorSim.hasWord(t.term)) {
                fTerms.add(t);
                terms.add(t.term);
            }
        }
        N = fTerms.size();        
    }
    
    // create symmetrix adjacency matrix with term similarities 
    private void createAdjMatrix() throws Exception {
        simMatrix = new double[N][N];
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) 
            if (i < j)
            {
                simMatrix[i][j] =
                        modifySim(vectorSim.compare(
                        wordToVectorSim.getWordVector(fTerms.get(i).term), 
                        wordToVectorSim.getWordVector(fTerms.get(j).term)
                        ));
            }
            else {
                if (i == j) simMatrix[i][j] = 1;
                else simMatrix[i][j] = simMatrix[j][i]; // i > j
            }
        }
        
    }
    
    private double modifySim(double sim) {
        if (Math.abs(sim) < 0.0000001) return 0;
        else { 
            if (simMod == SimMod.EXP) return Math.exp(sim);
            else if (simMod == SimMod.NONE) return sim;
            else if (simMod == SimMod.SQRT) return Math.sqrt(sim);
            else if (simMod == SimMod.ZERO_ONE) return 1;
            else throw new UnsupportedOperationException();
        }
    }
    
    private void calculateTfidf() {
        TfIdfTermWeight tfidf = new TfIdfTermWeight(fTerms, tdf);
        tfidfTerms = new ArrayList<WeightedTerm>(fTerms.size());        
        for (WeightedTerm ft : fTerms) {
            // create term with tf-idf weight
            WeightedTerm tt = new WeightedTerm(ft.term, tfidf.weight(ft.term));
            tfidfTerms.add(tt);
        }
        
    }
    
    // calculate page rank using R
    private void calculatePageRank() throws Exception {
        Rengine rengine = REngineManager.getRengine();
        File prScript = new File(KpeConfig.getProperty("pagerank.script"));
        if (!prScript.exists()) throw new RuntimeException("can't find pagerank script");
        // set working directory to folder of the script
        rengine.eval(String.format("setwd(\"%s\")", prScript.getParent()));
        // assign tfidf as personalized rank
        if (tdf != null) {
            double [] tfidf = new double[tfidfTerms.size()];
            for (int i = 0; i < tfidfTerms.size(); ++i) {
                tfidf[i] = tfidfTerms.get(i).weight;
            }
            rengine.assign("persRank", tfidf);
        }
        else rengine.eval("persRank <- NULL");
        // assign matrix
        double [] flatMatrix = new double[N*N];
        int c = 0;
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) flatMatrix[c++] = simMatrix[i][j];
        }        
        rengine.assign("flatMatrix", flatMatrix);
        rengine.eval(String.format("N <- %d", N));
        // assign damping factor
        rengine.eval(String.format("dampingF <- %f", dampingFactor));
        // run script
        rengine.eval( String.format("source(\"%s\")",prScript.getName()) );
        // read result from variable "result"
        REXP result = rengine.eval("result");
        pageRank = result.asDoubleArray();
        // populate prTerms with page rank values
        prTerms = new ArrayList<WeightedTerm>(N);        
        for (int i = 0; i < pageRank.length; ++i) {
            prTerms.add(new WeightedTerm(fTerms.get(i).term, pageRank[i]));            
        }                 
    }
    
    public String getId() {
        String id = "prank";        
        id += vectorSim.getId();        
        if (aggMethod == Method.SUM) id += "sum";
        else if (aggMethod == Method.MAX) id += "max";
        else throw new UnsupportedOperationException();
                
        return id;
    }

}
