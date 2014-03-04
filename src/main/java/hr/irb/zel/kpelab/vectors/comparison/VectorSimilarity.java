package hr.irb.zel.kpelab.vectors.comparison;

import hr.irb.zel.kpelab.vectors.IRealVector;

/** Basic measures of vector similarity. */
public class VectorSimilarity implements IVectorComparison {

    public enum SimilarityMeasure { L2_NEGATE, COSINE_SCALED, COSINE_CUTOFF, COSINE, EBE_MULTIPLY };
    
    private SimilarityMeasure measure; // measure for calculating similarity
    
    public VectorSimilarity(SimilarityMeasure m) { measure = m; }
    
    public double compare(IRealVector v1, IRealVector v2) {
        switch (measure) {
            case L2_NEGATE : return l2negate(v1, v2);
            case COSINE_SCALED : return cosineScaled(v1, v2);
            case COSINE_CUTOFF : return cosineCutoff(v1, v2);
            case EBE_MULTIPLY : return v1.dotProduct(v2);
            case COSINE : return v1.cosine(v2);
            default: throw new UnsupportedOperationException("unsupported measure");
        }
    }

     public String getId() { 
        switch (measure) {
            case L2_NEGATE : return "l2neg";
            case COSINE_SCALED : return "cosScaled";
            case COSINE_CUTOFF : return "cosCutoff";
            case EBE_MULTIPLY : return "ebe";
            case COSINE : return "cos";
            default: throw new UnsupportedOperationException("unsupported measure");
        }         
     }
    
    // - l2 distance
    private double l2negate(IRealVector v1, IRealVector v2) {
        return -1.0 * v1.subtract(v2).l2Norm();
    }

    // cosine distance linearly scaled form [-1,1] to [0,1]
    private double cosineScaled(IRealVector v1, IRealVector v2) {
        double cos = v1.cosine(v2);
        return 0.5 * (cos+1);
    }

    // cosine distance, 0 if <=0, normal if > 0
    private double cosineCutoff(IRealVector v1, IRealVector v2) {
        double cos = v1.cosine(v2);
        if (cos <= 0) return 0.;
        else return cos;        
    }    
    
}
