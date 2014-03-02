package hr.irb.zel.kpelab.vectors.document;

import hr.irb.zel.kpelab.util.IComponent;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;

/** Creates vector representation of a document.  */
public interface IDocumentVectorizer extends IComponent {
    
    public IRealVector vectorize(String text) throws Exception;
    public void setVectors(IWordToVectorMap wvmap);
    
}
