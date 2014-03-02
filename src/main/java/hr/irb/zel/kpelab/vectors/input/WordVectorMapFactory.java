package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.config.KpeConfig;

public class WordVectorMapFactory {

    private static WordToVectorDiskMap wikiLSIvectors;
    private static WordToVectorDiskMap wikiESAvectors;
    private static WordToVector01Filter wikiESA01vectors;
    
    public static WordToVectorMemMap getCwEmbeddings() throws Exception {
        WordToVectorMemMap wvm = new WordToVectorMemMap(
                KpeConfig.getProperty("cwe.words"), KpeConfig.getProperty("cwe.vectors"), "cwe");        
        return wvm;
    }
    
    public static WordToVectorDiskMap getLSIVectors() throws Exception {
        if (wikiLSIvectors == null) {            
            wikiLSIvectors = new WordToVectorDiskMap(
                    KpeConfig.getProperty("wikilsi.words"), 
                    KpeConfig.getProperty("wikilsi.vectors"),"lsi", true, false);            
        }
        return wikiLSIvectors;
    }

    public static WordToVectorDiskMap getESAVectors() throws Exception {
        if (wikiESAvectors == null) {            
            wikiESAvectors = new WordToVectorDiskMap(
                    KpeConfig.getProperty("wikiesa.words"), 
                    KpeConfig.getProperty("wikiesa.vectors"),"esa", true, true);            
        }
        return wikiESAvectors;
    }    
    
    public static WordToVector01Filter getESA01Vectors() throws Exception {
        if (wikiESA01vectors == null) {            
            wikiESA01vectors = new WordToVector01Filter(getESAVectors());
        }
        return wikiESA01vectors;
    }    
        
    /** Perform any actions necessary before the application ends. */
    public static void closeFactory() throws Exception {
        if (wikiLSIvectors != null) { wikiLSIvectors.saveCache(); }
        if (wikiESAvectors != null) { wikiESAvectors.saveCache(); }
    }
    
}
