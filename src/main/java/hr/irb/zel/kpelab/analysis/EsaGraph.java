/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.analysis;

import edu.stanford.nlp.parser.lexparser.Extractor;
import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.vectors.comparison.VectorSimilarity;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Outputs data and statistics of word x word graph with ESA vectors similarity.
 */
public class EsaGraph {
    
    private IWordToVectorMap wordToVector;    
    private VectorSimilarity vecSim;
    private TermExtractor termExtr;
    private List<String> terms;
    private double [][] simMatrix;
    private int N;
    private String docId;
    private String outFolder;
    
    public EsaGraph(IWordToVectorMap wvmap, VectorSimilarity vs)  {
        wordToVector = wvmap; vecSim = vs;
        outFolder = KpeConfig.getProperty("esa.graph");
    }
    
    private void createTermExtractor() throws UIMAException {
        if (termExtr == null) {
        termExtr = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, CanonicForm.STEM));         
        }        
    }
    
    public void processDocument(String text, String id) throws UIMAException, Exception {
        createTermExtractor();
        docId = id;
        List<String> allTerms = termExtr.extract(text);
        terms = new ArrayList(allTerms.size());
        for (String t : allTerms) {
            if (wordToVector.hasWord(t)) terms.add(t);
        }
        N = terms.size();
        simMatrix = new double[N][N];
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) 
            if (i < j)
            {
                simMatrix[i][j] = vecSim.compare(
                        wordToVector.getWordVector(terms.get(i)), 
                        wordToVector.getWordVector(terms.get(j))
                        );
            }
            else {
                if (i == j) simMatrix[i][j] = 1;
                else simMatrix[i][j] = simMatrix[j][i]; // i > j
            }
        }
    }
    
    public void outputMatrix() throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(outFolder+docId+"_matrix.txt"));
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                w.write(simMatrix[i][j] + " ");
            }
            w.write("\n");
        }
        w.close();
    }
    
    public void outputTerms() throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(outFolder+docId+"_terms.txt"));
        for (String t : terms) {
            w.write(t+"\n");
        }
        w.close();
    }
    
}
