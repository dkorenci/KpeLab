/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.evaluation;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class F1Evaluator {

    private IKpextractor extractor;    
    
    public F1Evaluator(IKpextractor extractor) {
        this.extractor = extractor;        
    }
    
    public F1Metric evaluateDocuments(List<KpeDocument> documents) throws Exception {       
        F1Metric result = new F1Metric();
        int stepCounter = 0;
        for (KpeDocument doc : documents) {                 
            F1Metric docResult = null;
            try {
                System.out.print("extracting for: " + doc.getId() + " ...");
                docResult = evaluateDocument(doc);
                System.out.println("done");
            }
            catch (Exception e) {
                Exception newe = new Exception("document: " + doc.getId(), e);    
                throw newe;
            }
            result.precision += docResult.precision;
            result.recall += docResult.recall;
            stepCounter++;
            if (stepCounter % 100 == 0) {
                F1Metric tmpResult = new F1Metric(result);
                tmpResult.precision /= stepCounter; tmpResult.recall /= stepCounter;
                tmpResult.calculateF1();
                System.out.println(tmpResult); 
            }
        }
        double N = documents.size();
        result.precision /= N; result.recall /= N;
        result.calculateF1();
        return result;
    }
    
    public F1Metric evaluateDocument(KpeDocument document) throws Exception {
        List<Phrase> solution = document.getKeyphrases();
        List<Phrase> result = this.extractor.extract(document);
        Set<Phrase> solSet = new TreeSet<Phrase>(solution);
        Set<Phrase> resSet = new TreeSet<Phrase>(result);
                
        double coveredSol = 0, coveredRes = 0;
        for (Phrase sol : solution) if (resSet.contains(sol)) coveredSol++;
        for (Phrase res : result) if (solSet.contains(res)) coveredRes++;
        
        F1Metric f1 = new F1Metric();
        if (result.size() == 0)  f1.precision = 0;
        else f1.precision = coveredRes / result.size();        
        if (solution.size() == 0) f1.recall = 0;
        else f1.recall = coveredSol / solution.size();        
        f1.calculateF1();
        
        return f1;
    }
    
}
