/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.evaluation;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality.PhEquality;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nu.xom.jaxen.expr.EqualityExpr;

/**
 *
 */
public class F1Evaluator {

    private IKpextractor extractor;    
    private IPhraseEquality equality;
    
    public F1Evaluator(IKpextractor extr, IPhraseEquality.PhEquality pheq) {
        extractor = extr;  
        if (pheq == PhEquality.CANONIC) equality = new CanonicPhraseEquality();
        else if (pheq == PhEquality.SEMEVAL ) equality = new SemevalPhraseEquality();
        else throw new UnsupportedOperationException("equality not defined");            
    }
    
    public F1Metric evaluateDocuments(List<KpeDocument> documents) throws Exception {       
        F1Metric result = new F1Metric();
        int stepCounter = 0;
        for (KpeDocument doc : documents) {                
            F1Metric docResult = null;
            try {
                //System.out.print("extracting for: " + doc.getId() + " ...");
                docResult = evaluateDocument(doc);
                //System.out.println("done");
            }
            catch (Exception e) {
                Exception newe = new Exception("document: " + doc.getId(), e);    
                throw newe;
            }
            result.precision += docResult.precision;
            result.recall += docResult.recall;
            result.f1 += docResult.f1;
            
            stepCounter++;
            if (stepCounter % 100 == 0) {                
                F1Metric tmpResult = new F1Metric(result);
                tmpResult.precision /= stepCounter; tmpResult.recall /= stepCounter;
                tmpResult.calculateF1();
                System.out.println("temporary result for " + stepCounter + 
                        " documents : " + tmpResult); 
            }
        }
        double N = documents.size();
        result.precision /= N; result.recall /= N;
        result.f1 /= N;
        return result;
    }
    
    public F1Metric evaluateDocument(KpeDocument document) throws Exception {
        List<Phrase> solution = document.getKeyphrases();
        List<Phrase> result = this.extractor.extract(document);
        return evaluateResult(result, solution);
    }
    
    public F1Metric evaluateResult(List<Phrase> result, List<Phrase> solution) 
            throws Exception {                
        double coveredSol = 0, coveredRes = 0;
        for (Phrase sol : solution) {
            for (Phrase res : result)
            if (equality.equal(res, sol)) { 
                coveredSol++;
                break;
            }
        }
        for (Phrase res : result) {
            for (Phrase sol : solution)
            if (equality.equal(res, sol)) {
                coveredRes++;
                break;
            }            
        }        
        
        F1Metric f1 = new F1Metric();
        if (result.isEmpty())  f1.precision = 0;
        else f1.precision = coveredRes / result.size();        
        if (solution.isEmpty()) f1.recall = 0;
        else f1.recall = coveredSol / solution.size();        
        f1.calculateF1();        
        
        f1.phrases = result;
        
        return f1;
    }    
    
}
