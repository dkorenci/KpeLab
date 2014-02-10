package hr.irb.zel.kpelab.vectorcoverage;

import hr.irb.zel.kpelab.vectors.IRealVector;
import java.util.List;

public interface ICoverageCalculator {
    public CoverageMeasures calculateCoverage(List<IRealVector> vectors) throws Exception;
}
