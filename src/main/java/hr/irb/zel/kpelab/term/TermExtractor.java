package hr.irb.zel.kpelab.term;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.jcas.JCas;

/** Extracts relevant terms from text. */
public class TermExtractor {
    
    private PosExtractorConfig config;
    private String text; // text from which to extract phrases
    private JCas jCas; // results from DKPro pipeline        
    List<WeightedTerm> wterms;

    
    
    public TermExtractor(PosExtractorConfig conf) { this.config = conf; }
    
    /** Extract terms, return list of non-duplicate terms. */
    public List<String> extract(String txt) throws UIMAException {
        text = txt;
        preprocess();
        extract();
        // return list of terms, dicarding weights
        List<String> terms = new ArrayList<String>(wterms.size());
        for (WeightedTerm wt : wterms) { terms.add(wt.term); }                
        return terms;
    }
    
    public List<WeightedTerm> extractWeighted(String txt) throws UIMAException {
        text = txt;
        preprocess();
        extract();
        return wterms;
    }    
    
    // run dkpro pipeline on the text
    private void preprocess() throws UIMAException {
        jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
        if (config.canonic == CanonicForm.LEMMA) 
            runPipeline(jCas, config.segmenter, config.lemmatizer, config.posTagger);                        
        else if (config.canonic == CanonicForm.STEM)
            runPipeline(jCas, config.segmenter, config.stemmer, config.posTagger);                        
        else throw new IllegalArgumentException("canonic form not covered");
    }    
    
    // extract canonic forms of word tokens that are nouns or adjectives    
    private void extract() {        
        Map<String, Integer> terms = new HashMap<String, Integer>();       
        // iterate over all tokens in the document jCas
        for (Token tok : select(jCas, Token.class)) {
            if (Utils.isWord(tok.getCoveredText()) == false) continue;
            if (isNoun(tok) == false && isAdj(tok) == false) continue;
            String term;            
            if (config.canonic == CanonicForm.LEMMA) term = tok.getLemma().getValue();
            else if (config.canonic == CanonicForm.STEM) term = tok.getStem().getValue();   
            else if (config.canonic == CanonicForm.NO_CANNONIZATION) term = tok.getCoveredText();
            else throw new IllegalArgumentException("unsupported canonic form");
            term = term.toLowerCase();
            if (terms.containsKey(term)) { // increse freq by 1
                terms.put(term, terms.get(term)+1);
            }
            else terms.put(term, 1);
        }
        wterms = new ArrayList<WeightedTerm>(terms.size());        
        for (Entry<String, Integer> e : terms.entrySet()) {
            wterms.add(new WeightedTerm(e.getKey(), e.getValue()));
        }
    }    

    private static boolean isNoun(Token tok) {
        return (tok.getPos() instanceof N);
    }

    private static boolean isAdj(Token tok) {
        return (tok.getPos() instanceof ADJ);
    }    
    
}
