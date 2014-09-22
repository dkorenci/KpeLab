package hr.irb.zel.kpelab.tfidf;

public interface ITfidfCalculator {
    
    double tfIdf(int tf, int docSize, int df, int numDocs);
    
}
