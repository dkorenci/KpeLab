package hr.irb.zel.kpelab.term;

public class WeightedTerm implements Comparable<WeightedTerm> {
    
    public WeightedTerm(String t, double w) { term = t; weight = w; }
    
    public String term;
    public double weight;

    public String toString() {
        return "weight: " + weight + " term: " + term;
    }
    
    public int compareTo(WeightedTerm wt) {
        int dc = Double.compare(weight, wt.weight);
        if (dc != 0) return -dc;
        else return term.compareTo(wt.term);
    }
}
