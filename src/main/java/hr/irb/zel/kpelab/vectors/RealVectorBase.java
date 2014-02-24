/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectors;

/** Base class with common functionality for vectors. */
public class RealVectorBase {

    private static final double EPSILON = 0.0000001;
    
    protected static boolean isZero(double d) {
        return Math.abs(d) < EPSILON;
    }
    
    protected static boolean equal(double d1, double d2) {
        return Math.abs(d1-d2) < EPSILON;
    }
    
}
