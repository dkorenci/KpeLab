package hr.irb.zel.kpelab.vectors;

import java.io.Serializable;
import org.apache.commons.math3.util.FastMath;

/**
 * Real vector implemented with an array of doubles.
 */
public class ArrayRealVector implements IRealVector, Serializable {
    
    private static final long serialVersionUID = 7080831865153494222L;

    private double [] vector;
    
    /** Create vector based on passed double array. */
    public ArrayRealVector(double [] vec) { vector = vec.clone(); }
    
    /** Create vector of size dim with zero elements. */
    public ArrayRealVector(int dim) {
        this(dim, 0.0);
    }
    
    /** Create vector of size dim with all values equal to val.  */
    public ArrayRealVector(int dim, double val) {
        vector = new double[dim];
        for (int i = 0; i < dim; ++i) vector[i] = val;
    }    
    
    public double element(int i) { return vector[i]; }

    public int dimension() { return vector.length; }

    public void setElement(int i, double val) { vector[i] = val; }

    private void checkDimensionMatch(IRealVector v) {
        if (v.dimension() != this.dimension()) 
            throw new IllegalArgumentException("vector dimensions does not match");
    }
    
    @Override
    public IRealVector clone() { return new ArrayRealVector(vector.clone()); }

    public IRealVector add(IRealVector v) {
        checkDimensionMatch(v);
        for (int i = 0; i < dimension(); ++i) vector[i] += v.element(i);
        return this;
    }
    
    public IRealVector subtract(IRealVector v) {
        checkDimensionMatch(v);
        for (int i = 0; i < dimension(); ++i) vector[i] -= v.element(i);
        return this;        
    }

    public double cosine(IRealVector v) {
        double dot = dotProduct(v), n1 = l2Norm(), n2 = v.l2Norm();
        if (n1 == 0 || n2 == 0) return 0;               
        return dot / (n1 * n2);
    }

    public double[] toArray() {
        return vector.clone();
    }

    public IRealVector ebeMultiply(IRealVector v) {
        checkDimensionMatch(v);
        for (int i = 0; i < dimension(); ++i) vector[i] *= v.element(i);
        return this;        
    }

    public double dotProduct(IRealVector v) {
        checkDimensionMatch(v);
        double dot = 0;
        for (int i = 0; i < dimension(); ++i) dot += (vector[i] * v.element(i));
        return dot;
    }

    public double l2Norm() {
        double norm = 0;
        for (int i = 0; i < dimension(); ++i) norm += vector[i]*vector[i];
        return FastMath.sqrt(norm);
    }

    public IRealVector maxMerge(IRealVector v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public VectorEntry[] getNonZeroEntries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double sumMinShared(IRealVector v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public IRealVector multiply(double alpha) {
        for (int i = 0; i < vector.length; ++i) vector[i] *= alpha;
        return this;
    }

}
