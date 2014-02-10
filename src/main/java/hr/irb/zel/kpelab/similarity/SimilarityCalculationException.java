/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.similarity;

/**
 *
 */
public class SimilarityCalculationException extends Exception {
    public SimilarityCalculationException(String message) {
        super(message);        
    }
    public SimilarityCalculationException(Exception exc) {
        super(exc);
    }    
}
