/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hr.irb.zel.kpelab.corpus.hulth;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hr.irb.zel.kpelab.phrase.PhraseHelper;
import hr.irb.zel.kpelab.phrase.PhraseHelper.TokenCanonic;
import org.apache.uima.UIMAException;

/**
 *
 */
public class DocumentReaderHulth {    
    
    private boolean readPhrases; // true if solution phrases are to be read
    private CanonicForm canonic; // canonic form for solution phrases
    
    public DocumentReaderHulth(boolean read, CanonicForm canon) {
        readPhrases = read; canonic = canon;
    }
    
    /** Read document named with full path or path relative to 
     * corpus location: SubdatasetFolder/FileName.ext */
    public KpeDocument readDocument(String docName) 
           throws FileNotFoundException, IOException, UIMAException {
        if (!docName.contains(CorpusHulth.corpusLocation)) 
            docName = CorpusHulth.corpusLocation + docName;
        File docFile = new File(docName);
        return readDocument(docFile);
    }    
    
    public KpeDocument readDocument(File docFile) 
           throws FileNotFoundException, IOException, UIMAException {
        KpeDocument doc = new KpeDocument();
        doc.setId(docFile.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new FileReader(docFile));        
        
        String line, title = "", body = "";
        boolean isTitle = true; int lineCounter = 1;
        while (true) {
            line = reader.readLine();
            if (isTitle) {
                if (line.startsWith("\t")) { // continuation of title after first line
                    title = title + "\n" + line ;
                }
                else {
                    if (lineCounter == 1) { // first line
                        title = title + line;
                    }                        
                    else {
                        isTitle = false;
                        title = title + ".";
                    }
                }
            }         
            
            if (isTitle == false) {
                if (line != null) body = body + "\n" + line;
                else {
                    if (body.endsWith(".") == false) {
                        body = body + ".";
                    }
                    break;
                }
            }      
            
            lineCounter++;
        }        
        reader.close();      
        
        if (readPhrases) doc.setKeyphrases(readPhrases(docFile));
        
        doc.setText(title + body);
        return doc;
    }
    
    // read assigned key phrases for the document
    private List<Phrase> readPhrases(File docFile) 
            throws FileNotFoundException, IOException, UIMAException {
        List<Phrase> phrases = new ArrayList<Phrase>();
        // replace extension to get phrase-file name
        String path = docFile.getAbsolutePath();
        path = path.replace(".abstr", ".uncontr"); 
        // read and concatenate lines
        BufferedReader reader = new BufferedReader(new FileReader(path));  
        String line, phraseStr = "";
        while ( (line = reader.readLine()) != null ) phraseStr = phraseStr + line;
        // create Phrase objects from phrases separated by ';', 
        // @TODO cannonize all tokens jointly
        String[] phraseTokens = phraseStr.split(";");
        for (String tok : phraseTokens) {
            Phrase phrase = new Phrase();            
            List<TokenCanonic> toklemmas = 
                    PhraseHelper.getCanonicForms(tok, canonic);
            List<String> tokens = new ArrayList<String>();
            List<String> lemmas = new ArrayList<String>();
            for (TokenCanonic tl : toklemmas) {
                tokens.add(tl.token);
                lemmas.add(tl.canonic);
            }
            phrase.setTokens(tokens);
            phrase.setCanonicTokens(lemmas);            
            phrase.setFrequency(-1);
            phrase.setFrequency(-1);            
            phrases.add(phrase);
        }
        return phrases;
    }
    
}

