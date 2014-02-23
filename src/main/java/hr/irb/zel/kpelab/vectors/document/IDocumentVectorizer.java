package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.vectors.IRealVector;

/** Creates vector representation of a document.  */
public interface IDocumentVectorizer {
    
    public IRealVector vectorize(String text) throws Exception;
    
}
