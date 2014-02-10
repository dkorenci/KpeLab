/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.tfidf;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.CorpusHulth;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import java.util.List;

/**
 * Methods that create and persist PhraseInDocumentsCounter for 
 * various datasets and settings.
 */
public class CounterCreation {

    public static final String DF_COUNTER_HULTH_ALL = "counts_hulthAllPhrases_StanfordNlpPos";
    public static final String DF_COUNTER_HULTH_TRAIN = "counts_hulthTrainPhrases_StanfordNlpPos";
    
    /** Test counter persistence by creating a counter from documents, 
     * reading a counter from file and comparing counts. */
    public static void testCounterPersistence() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getDocuments("Training", false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseInDocumentsCounter counter = new PhraseInDocumentsCounter(docs, extractor);
         String file = "hulthAllPhrases_OpenNlpPos";
         counter.saveToFile(file);
         
         KpeDocument doc = 
          new DocumentReaderHulth(true, CanonicForm.LEMMA).readDocument("Training/1037.abstr");
         List<Phrase> phrases = extractor.extractPhrases(doc.getText());
         for (Phrase ph : phrases) System.out.println(ph + " : " + counter.countOccurences(ph));
                                 
         counter = new PhraseInDocumentsCounter(file);
         for (Phrase ph : phrases) System.out.println(ph + " : " + counter.countOccurences(ph));
     }   

     public static void createCounterHulthAllDocs() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getAllDocuments(false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseInDocumentsCounter counter = new PhraseInDocumentsCounter(docs, extractor);
         String file = DF_COUNTER_HULTH_ALL;
         counter.saveToFile(file);         
     }      

     public static void createCounterHulthTrainingDocs() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getDocuments("Training", false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseInDocumentsCounter counter = new PhraseInDocumentsCounter(docs, extractor);
         String file = DF_COUNTER_HULTH_TRAIN;
         counter.saveToFile(file);         
     }      
     
}
