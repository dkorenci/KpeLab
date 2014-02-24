package hr.irb.zel.kpelab.vectors.comparison;

import hr.irb.zel.kpelab.vectors.IRealVector;

/** Compare two vectors by some criterion, return double as result. */
public interface IVectorComparison {

    public double compare(IRealVector v1, IRealVector v2);
    
}
