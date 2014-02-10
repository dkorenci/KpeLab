package hr.irb.zel.kpelab.vectors.similarity;

import hr.irb.zel.kpelab.vectors.IRealVector;

public interface IVectorSimilarity {

    public double similarity(IRealVector v1, IRealVector v2);
    
}
