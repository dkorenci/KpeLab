/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.evaluation;

/**
 *
 */
public class F1Metric {

    public double precision, recall, f1;
    
    public F1Metric() {
        precision = 0; recall = 0; f1 = 0;                
    }

    public F1Metric(F1Metric m) {
        precision = m.precision; recall = m.recall; f1 = m.f1;                
    }    
    
    public void calculateF1() {
        if (precision == 0 || recall == 0) f1 = 0;
        else f1 = 2 * (precision * recall) / (precision + recall);
    }
    
    public String toString() {
        String result; 
        result = "p: " + String.format("%.4f",precision);
        result += " , r: " + String.format("%.4f", recall);
        result += " , f1: " + String.format("%.4f", f1);
        return result;
    }
    
}
