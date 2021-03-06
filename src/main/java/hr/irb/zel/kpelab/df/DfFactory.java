/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.df;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.hulth.CorpusHulth;
import hr.irb.zel.kpelab.corpus.hulth.DocumentReaderHulth;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig.Components;
import hr.irb.zel.kpelab.term.TermExtractor;
import java.io.IOException;
import java.util.List;

/**
 * Methods that create and persist PhraseInDocumentsCounter for 
 * various datasets and settings.
 */
public class DfFactory {

    public static final String DF_COUNTER_HULTH_ALL = "counts_hulthAllPhrases_StanfordNlpPos";
    public static final String DF_COUNTER_HULTH_TRAIN = "counts_hulthTrainPhrases_StanfordNlpPos";
    
    // folder for saving counters
    private static final String repositoryFolder = KpeConfig.getProperty("cache.folder"); 
    
    private static String getFullPath(String fileId) { return  repositoryFolder+"/"+fileId; }
    
    /** Test counter persistence by creating a counter from documents, 
     * reading a counter from file and comparing counts. */
    public static void testCounterPersistence() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getDocuments("Training", false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseDocumentFrequency counter = 
                 new PhraseDocumentFrequency(PhraseDocumentFrequency.textList(docs), extractor);
         String file = "hulthAllPhrases_OpenNlpPos";
         counter.saveToFile(getFullPath(file));
         
         KpeDocument doc = 
          new DocumentReaderHulth(true, CanonicForm.LEMMA).readDocument("Training/1037.abstr");
         List<Phrase> phrases = extractor.extractPhrases(doc.getText());
         for (Phrase ph : phrases) System.out.println(ph + " : " + counter.countOccurences(ph));
                                 
         counter = new PhraseDocumentFrequency(getFullPath(file));
         for (Phrase ph : phrases) System.out.println(ph + " : " + counter.countOccurences(ph));
     }   

     public static void createCounterHulthAllDocs() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getAllDocuments(false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseDocumentFrequency counter = 
                 new PhraseDocumentFrequency(PhraseDocumentFrequency.textList(docs), extractor);
         String file = DF_COUNTER_HULTH_ALL;
         counter.saveToFile(getFullPath(file));         
     }      

     public static void createCounterHulthTrainingDocs() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getDocuments("Training", false, CanonicForm.LEMMA);
         PosRegexPhraseExtractor extractor = new PosRegexPhraseExtractor(CanonicForm.LEMMA);
         PhraseDocumentFrequency counter = 
                 new PhraseDocumentFrequency(PhraseDocumentFrequency.textList(docs), extractor);
         String file = DF_COUNTER_HULTH_TRAIN;
         counter.saveToFile(getFullPath(file));         
     }      

     public static void createDfSemevalStemOpenNlpTrain() throws Exception {
         List<KpeDocument> docs = CorpusSemeval.getTrain(SolutionPhraseSet.AUTHOR);
         TermExtractor extractor = new TermExtractor(
                 new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
         TermDocumentFrequency df = new TermDocumentFrequency(extractor);
         df.createTermFrequency(docs, "df_semeval_stem_opennlp_train");
     }
     
     public static void createDfSemevalStemOpenNlpTrainTrial() throws Exception {
         List<KpeDocument> docs = CorpusSemeval.getTrain(SolutionPhraseSet.AUTHOR);
         docs.addAll(CorpusSemeval.getTrial(SolutionPhraseSet.AUTHOR));
         TermExtractor extractor = new TermExtractor(
                 new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
         TermDocumentFrequency df = new TermDocumentFrequency(extractor);
         df.createTermFrequency(docs, "df_semeval_stem_opennlp_train_trial");
     }     
     
     public static TermDocumentFrequency loadDfSemevalStemOpenNlpTrainTrial() throws Exception {
         return new TermDocumentFrequency("df_semeval_stem_opennlp_train_trial");
     }       
     
     public static TermDocumentFrequency loadDfSemevalStemOpenNlpTrain() throws Exception {
         return new TermDocumentFrequency("df_semeval_stem_opennlp_train");
     }     
     
     public static void createDfSemevalStemOpenNlpAll() throws Exception {
         List<KpeDocument> docs = CorpusSemeval.getAll(SolutionPhraseSet.AUTHOR);
         TermExtractor extractor = new TermExtractor(
                 new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
         TermDocumentFrequency df = new TermDocumentFrequency(extractor);
         df.createTermFrequency(docs, "df_semeval_stem_opennlp_all");
     }
     
     public static TermDocumentFrequency loadDfSemevalStemOpenNlpAll() throws Exception {
         return new TermDocumentFrequency("df_semeval_stem_opennlp_all");
     }

     public static void createDfHulthStemOpenNlp() throws Exception {
         List<KpeDocument> docs = CorpusHulth.getAllDocuments(false, CanonicForm.STEM);
         TermExtractor extractor = new TermExtractor(
                 new PosExtractorConfig(Components.OPEN_NLP, CanonicForm.STEM));
         TermDocumentFrequency df = new TermDocumentFrequency(extractor);
         df.createTermFrequency(docs, "df_hulth_stem_opennlp");
     }
     
     public static TermDocumentFrequency loadDfHulthStemOpenNlp() throws Exception {
         return new TermDocumentFrequency("df_hulth_stem_opennlp");
     }     
     
}
