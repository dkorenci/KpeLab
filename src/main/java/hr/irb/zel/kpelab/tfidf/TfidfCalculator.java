package hr.irb.zel.kpelab.tfidf;

/** Standard tfidf. */
public class TfidfCalculator implements ITfidfCalculator {

    public double tfIdf(int tf, int docSize, int df, int numDocs) {
        return (tf/(double)docSize) * -1 * Math.log(df/(double)numDocs);
    }

}
