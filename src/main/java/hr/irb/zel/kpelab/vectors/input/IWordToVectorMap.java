package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.IOException;

/**
 * Produces vectors of real numbers for words.
 */
public interface IWordToVectorMap {
    
    public IRealVector getWordVector(String word) throws Exception;
    public boolean hasWord(String word);
    
}
