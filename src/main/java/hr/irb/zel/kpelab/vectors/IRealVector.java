package hr.irb.zel.kpelab.vectors;

import java.io.Serializable;

/**
 * Common functionality for all vectors. 
 * Binary vector operations must alter this vector and return this.
 */
public interface IRealVector extends Serializable {

    /** Return the element of the vector at dimension i. */
    public double element(int i);
    
    /** Return the size of the vector. */
    public int dimension();
    
    /** Set i-th coordinate to val. */
    public void setElement(int i, double val);
    
    /** Return exact copy of the vector. */
    public IRealVector clone();
    
    /** True if values at all coordinates are zero. */
    public boolean isZero();
    
    @Override
    public boolean equals(Object o);
    
    /** Add v to this vector element by element, return this. */
    public IRealVector add(IRealVector v);    
    
    /** Add v's coordinates to this, for each shared coordinate set maximum of v and this. */
    public IRealVector maxMerge(IRealVector v);    
    
    /** Subtract v from this vector element by element, return this. */
    public IRealVector subtract(IRealVector v);
    
    /** Calculate dot product of this and v. */
    public double dotProduct(IRealVector v);
    
    /** Return l2 (euclid) norm of this. */
    public double l2Norm();
    
    /** Calculate cosine similarity between this and v. */
    public double cosine(IRealVector v);
    
    /** Sum of min. values at shared coordinates. */
    public double sumMinShared(IRealVector v);
    
    /** Return this vector as array of doubles. */
    public double[] toArray();
    
    /** Perform element by element multiplication of this and v. Return v. */
    public IRealVector ebeMultiply(IRealVector v);   
    
    /** Get (coordinate, value) pairs where value is not zero. */
    public VectorEntry[] getNonZeroEntries();
    
    /** Multiply every vector element by alpha. Return this. */
    public IRealVector multiply(double alpha);
    
}
