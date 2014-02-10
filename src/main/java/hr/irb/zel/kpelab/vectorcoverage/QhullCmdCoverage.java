package hr.irb.zel.kpelab.vectorcoverage;

import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


/** Calculates coverage via qhull command line invocation. */
public class QhullCmdCoverage implements ICoverageCalculator {
  
    private static final String command = "/home/dam1root/software/qhull-2012.1/bin/qconvex";
    private static final String args = "FS";
    
    public CoverageMeasures calculateCoverage(List<IRealVector> vectors) 
            throws IOException, QhullException {
        Process process = Runtime.getRuntime().exec(command + " " + args);
        BufferedReader procOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        OutputStreamWriter procIn = new OutputStreamWriter(process.getOutputStream());
                
        StringBuilder input = new StringBuilder();
        int D = vectors.get(0).dimension();
        // write dimension of vectors
        input.append(Integer.toString(D)).append("\n"); 
        // write number of vectors
        input.append(Integer.toString(vectors.size())).append("\n"); 
        // write vectors, one per line
        for (IRealVector vec : vectors) {             
            for (double c : vec.toArray()) input.append(String.format("%.15f ", c));
            input.append("\n");
        }        
//        System.out.println("input: \n" + input.toString()); 
        // send input to process
        procIn.append(input.toString()).flush();
        procIn.close();
        // read and process output of the process
        procOut.readLine(); // skip first line
        String result = procOut.readLine(); procOut.close();
        if (result == null) throw new QhullException("second line of output missing");
  //      System.out.println("process output string: " + result);        
        // parse output
        String [] tokens = result.split("\\s");
        if (tokens.length != 3) throw new QhullException("wrong number or tokens on second line");        
        CoverageMeasures measures = new CoverageMeasures();
        measures.area = Double.valueOf(tokens[1]);
        measures.volume = Double.valueOf(tokens[2]);        
        //System.out.println("process output parsed: \n" + measures.area + " " + measures.volume);
        //System.out.println("process exit value: " + process.exitValue());        
        return measures;
    }

    
    
}
