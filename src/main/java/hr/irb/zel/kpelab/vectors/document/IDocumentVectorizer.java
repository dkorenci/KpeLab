package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;

/** Creates vector representation of a document.  */
public interface IDocumentVectorizer {
    
    public IRealVector vectorize(String text) throws Exception;
    public void setVectors(IWordToVectorMap wvmap);
    
}
