package hr.irb.zel.kpelab.corpus.semeval;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.term.TermExtractor;
import java.util.List;

/** Tests for probing corpus structure. */
public class CorpusSemevalTests {
    
    public static void printTerms(String docId) throws Exception {
        KpeDocument doc = CorpusSemeval.getDocument(docId, SolutionPhraseSet.AUTHOR);
        TermExtractor extractor = new TermExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.LEMMA));
        List<String> terms = extractor.extract(doc.getText());
        int w = 0;
        for (String t : terms) {
            System.out.print(t+" ");
            w++; if (w % 7 == 0) System.out.println();
        }
        System.out.println();
    }
    
}
