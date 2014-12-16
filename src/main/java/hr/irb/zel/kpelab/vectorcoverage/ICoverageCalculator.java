package hr.irb.zel.kpelab.vectorcoverage;

import vectors.IRealVector;
import java.util.List;

public interface ICoverageCalculator {
    public CoverageMeasures calculateCoverage(List<IRealVector> vectors) throws Exception;
}
