/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectorcoverage;

/**
 *
 */
public class CoverageMeasures {

    public double volume, area;
    
    public String toString() {
        return String.format("volume %.10f , area: %.10f", volume, area);
    }
    
}
