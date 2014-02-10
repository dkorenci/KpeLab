/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.vectors;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TIntDoubleProcedure;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Formatter;
import org.apache.commons.math3.util.FastMath;

/**
 * Class optimized for high dimensional vectors with few non-zero elements.
 * Non-zero elements are kept in int-to-double map, all the other elements
 * are assumed to be equal to 0.0
 */
public class SparseRealVector implements IRealVector, Serializable {
    
    private static final long serialVersionUID = 2004194095001077736L;

    private int dim;
    private TIntDoubleMap map;
    
    /** Init vector of size d with all zero elements. */
    public SparseRealVector(int d) { 
        dim = d; 
        initMap();
    }
    
    public SparseRealVector(double [] values) {
        dim = values.length;
        initMap();
        for (int i = 0; i < dim; ++i) map.put(i, values[i]);
    }
    
    @Override
    public String toString() {
        if (map.size() == 0) return dim + " zeroes";
        StringBuilder builder = new StringBuilder();
        int [] keys = map.keys(); Arrays.sort(keys);
        builder.append("num.non.zero ").append(keys.length).append(" : ");
        for (int k : keys) {
            String val = String.format("%.5f", map.get(k));
            builder.append(k).append(" ").append(val).append(" ");
        }
        return builder.toString();
    }
   
    
    /** Constructor for cloning. */
    private SparseRealVector(int d, TIntDoubleMap m) {
        dim = d; map = m;
    }
    
    private void initMap() { map = new TIntDoubleHashMap(); }    
    
    // check if index is valid for this vector
    private void checkIndex(int i) {
        if (i < 0 || i >= dim) throw 
            new IndexOutOfBoundsException("index: " + i + " , dimension: " + dim);
    }
    
    // check if dimension of the vector v matches dimension of this
    private void checkDimension(IRealVector v) {
        if (dimension() != v.dimension()) throw
                new IllegalArgumentException("vector dimensions do not match");            
    }
    
    // check if type of the vector is sparse real vector
    private void checkType(IRealVector v) {
        if (v instanceof SparseRealVector == false) throw
                new UnsupportedOperationException(
                "argument must be of type SparseRealVector");
    }   
    
    public double element(int i) {
        checkIndex(i);
        if (map.containsKey(i)) return map.get(i);
        else return 0.0;
    }

    public int dimension() { return dim; }

    public void setElement(int i, double val) {
        checkIndex(i);
        map.put(i, val);
    }

    public IRealVector clone() {
        return new SparseRealVector(dim, new TIntDoubleHashMap(map));
    }

    public IRealVector add(IRealVector v) {
        checkDimension(v); checkType(v);
        SparseRealVector vec = (SparseRealVector)v;
        vec.map.forEachEntry(new TIntDoubleProcedure() {
            public boolean execute(int i, double d) {
                double val;
                if (map.containsKey(i)) val = map.get(i);
                else val = 0;                
                map.put(i, val + d);                
                return true;
            }
        });
        return this;
    }

    public IRealVector subtract(IRealVector v) {
        checkDimension(v); checkType(v);
        SparseRealVector vec = (SparseRealVector)v;
        vec.map.forEachEntry(new TIntDoubleProcedure() {
            public boolean execute(int i, double d) {
                double val;
                if (map.containsKey(i)) val = map.get(i);
                else val = 0;                
                map.put(i, val - d);                
                return true;
            }
        });        
        return this;
    }

    public double dotProduct(IRealVector v) {
        checkDimension(v); checkType(v);
        SparseRealVector vec = (SparseRealVector)v;
        int [] thiskeys = map.keys();
        int [] vkeys = vec.map.keys();
        // determine and set smaller and larger map and smaller set of keys
        TIntDoubleMap msmall, mlarge; int [] keys = null;       
        if (thiskeys.length > vkeys.length) { 
            msmall = vec.map; keys = vkeys; mlarge = this.map;
        }
        else { 
            msmall = this.map; keys = thiskeys; mlarge = vec.map; 
        }
        // iterate over smaller set of keys and calculate product        
        double product = 0;
        for (int i = 0; i < keys.length; ++i) {
            double val = msmall.get(keys[i]);
            if (mlarge.containsKey(keys[i])) {
                product += val * mlarge.get(keys[i]);
            }
        }
        return product;
    }

    
    private double norm; // auxiliary field for calcuating vector norm
    public double l2Norm() {
        norm = 0;
        map.forEachValue(new TDoubleProcedure() {
            public boolean execute(double d) {
                norm += d * d;
                return true;
            }
        });
        return FastMath.sqrt(norm);
    }

    public double cosine(IRealVector v) {
        checkDimension(v); checkType(v);       
        double dot = dotProduct(v), n1 = l2Norm(), n2 = v.l2Norm();
        if (n1 == 0 || n2 == 0) return 0;
        return dot / (n1 * n2);
    }

    public double[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public IRealVector ebeMultiply(IRealVector v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
