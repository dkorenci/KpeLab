package hr.irb.zel.kpelab.corpus.semeval;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.experiments.SemevalCorpusExperiments;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.term.TermExtractor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    
    // for a set of documents, write phrases uncovered by the keyphrase extractor
    
    public static void coverageErrors() throws Exception {        
        List<KpeDocument> docs = CorpusSemeval.getAll(SolutionPhraseSet.COBINED);
        final String outputFolder = CorpusSemeval.corpusLocation + "coverageErrors/";
        IPhraseExtractor phExtr = new PosRegexPhraseExtractor(
                new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));        
        for (KpeDocument doc : docs) {
            Set<Phrase> text = new TreeSet<Phrase>(phExtr.extractPhrases(doc.getText()));
            List<Phrase> gold = doc.getKeyphrases();
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outputFolder+doc.getId()+".coverr.txt"));
            writer.write("---not covered:\n");
            for (Phrase ph : gold) {
                if (!text.contains(ph)) writer.write(ph.canonicForm()+"\n");
            }
            writer.write("---covered:\n");
            for (Phrase ph : gold) {
                if (text.contains(ph)) writer.write(ph.canonicForm()+"\n");
            }
            writer.write("---text phrases:\n");
            for (Phrase ph : text) writer.write(ph.canonicForm()+"\n");
            writer.close();
        }
    }
    
}
