package hr.irb.zel.kpelab.vectors.input;

import hr.irb.zel.kpelab.util.IComponent;
import hr.irb.zel.kpelab.vectors.IRealVector;
import java.io.IOException;

/**
 * Produces vectors of real numbers for words.
 */
public interface IWordToVectorMap extends IComponent {
    
    public IRealVector getWordVector(String word) throws Exception;
    public boolean hasWord(String word);
    
}
