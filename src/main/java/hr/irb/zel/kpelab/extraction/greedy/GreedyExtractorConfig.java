package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.util.IComponent;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.TermSetPruneFilter;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Processing components of GreedyExtractor and other configuration.
 */
public class GreedyExtractorConfig implements IComponent {
    
    public IDocumentVectorizer docVectorizer;
    public IPhraseSetVectorizer phVectorizer;
    public IVectorComparison phraseSetQuality;
    public IPhraseExtractor phraseExtractor;    
    public IWordToVectorMap wordToVector;
    public VectorMod modification;
    public CanonicForm cform;
    private TermExtractor termExtractor;

    // vector modification to be applied before extraction starts
    public enum VectorMod { 
        NONE, 
        PRUNE_UNIQUE // remove from vectors those components that are not
        // shared with at least another word from document
    }
    
    public GreedyExtractorConfig(IDocumentVectorizer dvec, IPhraseExtractor phext, 
            IPhraseSetVectorizer phvec, IVectorComparison cmp) {  
        this(null, VectorMod.NONE, CanonicForm.STEM, dvec, phext, phvec, cmp);
//        docVectorizer = dvec; phVectorizer = phvec; 
//        phraseSetQuality = cmp; phraseExtractor = phext;        
    }
    
    public GreedyExtractorConfig(IWordToVectorMap wvmap, VectorMod mod, CanonicForm cf,
            IDocumentVectorizer dvec, IPhraseExtractor phext, 
            IPhraseSetVectorizer phvec, IVectorComparison cmp) {    
        wordToVector = wvmap; modification = mod; cform = cf;
        docVectorizer = dvec; phVectorizer = phvec; 
        phraseSetQuality = cmp; phraseExtractor = phext;        
        termExtractor = null;
    }
    
    public String getId() {
        String id = "greedy";
        if (modification == VectorMod.NONE) id += "."+wordToVector.getId();
        else if (modification == VectorMod.PRUNE_UNIQUE) id += "."+wordToVector.getId()+"Pr";
        id += "."+docVectorizer.getId();
        id += "."+phVectorizer.getId();
        id += "."+phraseSetQuality.getId();        
        return id;        
    }
    
    // adapt vectorizers to document text
    public void adaptToDocument(String text) throws ResourceInitializationException, 
            UIMAException, Exception {
        if (modification == VectorMod.NONE) return;
        if (termExtractor == null) {
            termExtractor = new TermExtractor(new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, cform));            
        }
        List<String> terms = termExtractor.extract(text);
        IWordToVectorMap pruneFilter = new TermSetPruneFilter(wordToVector, terms);
        docVectorizer.setVectors(pruneFilter);
        phVectorizer.setVectors(pruneFilter);        
    }
    
}
