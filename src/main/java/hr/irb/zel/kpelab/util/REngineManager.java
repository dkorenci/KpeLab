package hr.irb.zel.kpelab.util;

import org.rosuda.JRI.Rengine;

/**
 * Creates, provides and ends Rengine.
 */
public class REngineManager {

    private static Rengine rengine;
    
    /** Initialize R. */
    public static Rengine getRengine() throws Exception {        
        if (rengine != null) return rengine;        
        //rengine = Rengine.getMainEngine();
        rengine = new Rengine (new String [] {"--vanilla"}, false, null);                     
        if (!rengine.waitForR()) {
            throw new Exception("R engine did not initialize");
        }
        return rengine;
    }      
    
    public static void closeRengine() {
        if (rengine != null) rengine.end();
    }
    
}
