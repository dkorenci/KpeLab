package hr.irb.zel.kpelab.term;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import org.apache.uima.jcas.JCas;

/** Extracts relevant terms from text. */
public class TermExtractor {
    
    private PosExtractorConfig config;
    private String text; // text from which to extract phrases
    private JCas jCas; // results from DKPro pipeline        
    List<String> result;
    private static final Pattern wordPattern =
        Pattern.compile("\\p{Alpha}+(\\-\\p{Alpha})?");
    
    
    public TermExtractor(PosExtractorConfig conf) { this.config = conf; }
    
    /** Extract terms, return list of non-duplicate terms. */
    public List<String> extract(String txt) throws UIMAException {
        text = txt;
        preprocess();
        extract();
        return result;
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
        Set<String> terms = new HashSet<String>();       
        // iterate over all tokens in the document jCas
        for (Token tok : select(jCas, Token.class)) {
            if (isWord(tok.getCoveredText()) == false) continue;
            if (isNoun(tok) == false && isAdj(tok) == false) continue;
            String term;            
            if (config.canonic == CanonicForm.LEMMA) term = tok.getLemma().getValue();
            else term = tok.getStem().getValue();   
            term = term.toLowerCase();
            terms.add(term);
        }
        result = new ArrayList<String>(terms);        
    }    

    private static boolean isWord(String token) {
        return wordPattern.matcher(token).matches();
    }

    private static boolean isNoun(Token tok) {
        return (tok.getPos() instanceof N);
    }

    private static boolean isAdj(Token tok) {
        return (tok.getPos() instanceof ADJ);
    }    
    
}
