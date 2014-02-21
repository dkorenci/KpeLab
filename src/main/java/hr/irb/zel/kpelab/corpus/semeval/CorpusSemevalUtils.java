package hr.irb.zel.kpelab.corpus.semeval;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import java.util.List;

public class CorpusSemevalUtils {

    // print all stemmed phrases for a document
    public static void outputStemmedPhrases(String docId) throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument(docId, SolutionPhraseSet.AUTHOR);
        PosRegexPhraseExtractor extr = new PosRegexPhraseExtractor(
         new PosExtractorConfig(PosExtractorConfig.Components.OPEN_NLP, CanonicForm.STEM));
        List<Phrase> phr = extr.extractPhrases(doc.getText());
        for (Phrase ph : phr) System.out.println(ph.canonicForm());        
    }
    
}
