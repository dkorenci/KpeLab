/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.analysis;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import java.util.Collection;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import org.apache.uima.jcas.JCas;

/**
 *
 */
public class PosTaggingAnalyser {

    private PosExtractorConfig config;
    private JCas jCas;
    
    public PosTaggingAnalyser(PosExtractorConfig conf) { config = conf; }

    public static void analyseDocument(KpeDocument doc, PosExtractorConfig config) 
            throws Exception {   
        PosTaggingAnalyser analyser = new PosTaggingAnalyser(config);
        analyser.processDocument(doc);
        analyser.printProcessedDocument();
    }    
    
    public void processDocument(KpeDocument doc) 
            throws UIMAException {
        jCas = JCasFactory.createJCas();
        jCas.setDocumentText(doc.getText());
        jCas.setDocumentLanguage("en");
        if (config.canonic == CanonicForm.LEMMA)
            runPipeline(jCas, config.segmenter, config.lemmatizer, config.posTagger);         
        else 
            runPipeline(jCas, config.segmenter, config.stemmer, config.posTagger);                 
    }
    
    public void printProcessedDocument() {
        Collection<Sentence> sentences = select(jCas, Sentence.class);
        System.out.println("num sentences: " + sentences.size());
        for (Sentence sentence : sentences) {            
            List<Token> tokens = selectCovered(jCas, Token.class, sentence);           
            for (Token token : tokens) {
                String t = token.getCoveredText();
                // get DkPro uima type
                String pos1 = token.getPos().getClass().getName();
                pos1 = pos1.substring(pos1.lastIndexOf(".")+1);
                // get value assigned by wrapped pos tagger
                String pos2 = token.getPos().getPosValue();
                String c; 
                if (config.canonic == CanonicForm.LEMMA) c = token.getLemma().getValue();
                else c = token.getStem().getValue();
                String s = " ";
                System.out.println(t+s+pos1+s+pos2+s+c+s);
            }
            System.out.println("EOS");
        }
    }
}
