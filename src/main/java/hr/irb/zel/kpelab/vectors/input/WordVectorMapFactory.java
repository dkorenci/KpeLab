package hr.irb.zel.kpelab.vectors.input;

public class WordVectorMapFactory {

    private static WordToVectorDiskMap wikiLSIvectors;
    private static WordToVectorDiskMap wikiESAvectors;
    
    public static WordToVectorMemMap getCwEmbeddings() throws Exception {
        WordToVectorMemMap wvm = new WordToVectorMemMap(
                "/data/datasets/word_vectors/senna3.0_embeddings/words.lst", 
                "/data/datasets/word_vectors/senna3.0_embeddings/embeddings.txt");        
        return wvm;
    }
    
    public static WordToVectorDiskMap getLSIVectors() throws Exception {
        if (wikiLSIvectors == null) {            
            wikiLSIvectors = new WordToVectorDiskMap(
                    "/data/datasets/word_vectors/wiki_lsi/wiki-words.txt", 
                    "/data/datasets/word_vectors/wiki_lsi/wiki-matrix.txt", true, false);            
        }
        return wikiLSIvectors;
    }

    public static WordToVectorDiskMap getESAVectors() throws Exception {
        if (wikiESAvectors == null) {            
            wikiESAvectors = new WordToVectorDiskMap(
                    "/data/datasets/word_vectors/wiki_esa/wiki-esa-terms.txt", 
                    "/data/datasets/word_vectors/wiki_esa/wiki-esa-vectors.txt", true, true);            
        }
        return wikiESAvectors;
    }    
    
    /** Perform any actions necessary before the application ends. */
    public static void closeFactory() throws Exception {
        if (wikiLSIvectors != null) { wikiLSIvectors.saveCache(); }
        if (wikiESAvectors != null) { wikiESAvectors.saveCache(); }
    }
    
}
