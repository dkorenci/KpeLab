package hr.irb.zel.kpelab.corpus.semeval;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorpusSemeval {

    public static final String corpusLocation = KpeConfig.getProperty("corpus.semeval");            
    
    /** Get "trial" subset of the dataset. */
    public static List<KpeDocument> getTrial(SolutionPhraseSet phSet) throws IOException {
        return getDataset("trial", phSet);
    }
    
    /** Get "valid" subset of the dataset. */
    public static List<KpeDocument> getTrain(SolutionPhraseSet phSet) throws IOException {
        return getDataset("train", phSet);
    }
    
    /** Get "trial" subset of the dataset. */
    public static List<KpeDocument> getTest(SolutionPhraseSet phSet) throws IOException {
        return getDataset("test", phSet);
    }    
    
    /** Get the entire dataset. */
    public static List<KpeDocument> getAll(SolutionPhraseSet phSet) throws IOException {
        List<KpeDocument> docs = new ArrayList<KpeDocument>();
        docs.addAll(getDataset("trial", phSet));
        docs.addAll(getDataset("train", phSet));
        docs.addAll(getDataset("test", phSet));
        return docs;
    }    
    
    /** Get subset of the dataset from specified folder: trial, train or test. */
    public static List<KpeDocument> getDataset(String folder, SolutionPhraseSet phSet) 
            throws IOException {
        
        DocumentToSolutionMap solMap = 
                new DocumentToSolutionMap(solutionFileName(folder, phSet));
        List<File> documents = getDocumentFiles(corpusLocation+folder);
        
        List<KpeDocument> result = new ArrayList<KpeDocument>();
        numMultSolutions = numSolutions = 0;
        for (File doc : documents) {
            String docText = readDocument(doc);
            String docId = getDocId(doc);
            List<Phrase> phrases = getPhrases(docText, solMap.getSolutions(docId));
            result.add(new KpeDocument(docId, docText, phrases));
        }
        System.out.println("num solutions / num multiple : "+
                (double)numMultSolutions/numSolutions);
        return result;
    }

    /** Get a single document specified as folder/docId */
    public static KpeDocument getDocument(String doc, String phSetId) 
            throws IOException {        
        File docFile = new File(corpusLocation + doc + ".txt.final");
        String folder = doc.split("/")[0];
        DocumentToSolutionMap solMap = 
                new DocumentToSolutionMap(solutionFileName(folder, phSetId));                
        String docText = readDocument(docFile);
        String docId = getDocId(docFile);                
        List<Phrase> phrases = getPhrases(docText, solMap.getSolutions(docId));
        return new KpeDocument(docId, docText, phrases);
    }    
    
    private static int numMultSolutions; // number of phrases with multiple solutions fomrs
    private static int numSolutions; // number of solution phrases 
    
    /** Get a single document specified as folder/docId */
    public static KpeDocument getDocument(String doc, SolutionPhraseSet phSet) 
            throws IOException {        
        return getDocument(doc, getPhraseSetId(phSet));
    }
    
    private static String getDocId(File f) {        
        return f.getName().split("\\.")[0];
    }
    
    private static List<File> getDocumentFiles(String folder) {
        // accept files with document text
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains(".txt.");
            }
        };
        return Arrays.asList(new File(folder).listFiles(filter));
    }
    
    private static String solutionFileName(String folder, SolutionPhraseSet phSet) {  
        return solutionFileName(folder, getPhraseSetId(phSet));
    }

    private static String getPhraseSetId(SolutionPhraseSet phSet) {
        String phSetId; 
        if (phSet == SolutionPhraseSet.AUTHOR) phSetId = "author";
        else if (phSet == SolutionPhraseSet.READER) phSetId = "reader";
        else phSetId = "combined";      
        return phSetId;
    }
    
    private static String solutionFileName(String folder, String phSetId) {
        return corpusLocation+folder+"/"+folder+"."+phSetId+".stem.final";
    }
    
    // From a list of solutions pick one of surface forms.
    private static List<Phrase> getPhrases(String docText, List<List<Phrase>> solutions) {
        List<Phrase> phrases = new ArrayList<Phrase>();
        numSolutions += solutions.size();
        for (List<Phrase> sol : solutions) {
            if (sol.isEmpty()) throw new IllegalArgumentException("empty solution");
            
            Phrase s = sol.get(0);
            phrases.add(sol.get(0));
            if (sol.size() > 1) s.setAlternativeForms(sol.subList(1, sol.size()));
            
//            else if (sol.size() == 1) phrases.add(sol.get(0));
//            else { 
//                numMultSolutions++;
//                // more than one surface form, pick one depending on their structure
//                boolean ofPhrase = false; // phrase of the form "A of B"
//                int notOfPhrase = 0; // index of a phrase that is not "A of B"
//                // search for "of phrases"
//                for (int i = 0; i < sol.size(); ++i) {
//                    Phrase ph = sol.get(i);
//                    if (ph.getCanonicTokens().contains("of")) ofPhrase = true;
//                    else notOfPhrase = i;
//                }
//                // selection of surface form
//                if (ofPhrase) phrases.add(sol.get(notOfPhrase));
//                else phrases.add(sol.get(0)); // add one of the forms
//            }
        }
        return phrases;
    }

    private static String readDocument(File doc) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(doc));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        return DocumentCleaner.cleanDocument(builder.toString());
    }
    
}
