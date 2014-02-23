package hr.irb.zel.kpelab.extraction.greedy;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.extraction.IKpextractor;
import hr.irb.zel.kpelab.phrase.CanonicForm;
import hr.irb.zel.kpelab.phrase.IPhraseExtractor;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.phrase.PosExtractorConfig;
import hr.irb.zel.kpelab.phrase.PosRegexPhraseExtractor;
import hr.irb.zel.kpelab.term.TermExtractor;
import hr.irb.zel.kpelab.util.VectorAggregator;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.input.IWordToVectorMap;
import hr.irb.zel.kpelab.vectors.input.WordVectorMapFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Greedy algorithm that in each step adds best phrase to the solution,
 * works with esa vectors, aggregates terms in the document and phrases 
 * in solution set by max, compares with sumMinShared vector operation. */
public class GreedyMaxExtractor implements IKpextractor {

    IPhraseExtractor phExtr;
    int K; // number of phrases to extract
    IRealVector docVector;
    KpeDocument doc;    
    List<Phrase> docPhrases;
    IRealVector[] phraseVectors;
    IWordToVectorMap wordVec;
    PosExtractorConfig config;
    
    public GreedyMaxExtractor(int k) {
        K = k;
    }
    
    public List<Phrase> extract(KpeDocument d) throws Exception {
        doc = d;
        config = new PosExtractorConfig(
                PosExtractorConfig.Components.OPEN_NLP, CanonicForm.STEM);
        wordVec = WordVectorMapFactory.getESAVectors();
        constructDocumentVector();
        processPhrases();
        return constructPhraseSet();        
    }

    // max. aggregate terms in the document
    private void constructDocumentVector() throws Exception {
        TermExtractor textr = new TermExtractor(config);
        List<String> terms = textr.extract(doc.getText());
        VectorAggregator agg = new VectorAggregator(wordVec);
        docVector = agg.aggregate(terms, VectorAggregator.Method.MAX);        
    }

    private List<Phrase> constructPhraseSet() {
        IRealVector sol = null;
        Set<Integer> optPhraseSet = new TreeSet<Integer>();
        // add K phrases, one by one
        for (int i = 0; i < K; i++) { 
            // choose phrase
            int optPhrase = -1;
            double optSol = -1;
            for (int j = 0; j < docPhrases.size(); ++j) {
            if (phraseVectors[j] != null)
            if (optPhraseSet.contains(j) == false) {
                IRealVector newSol;
                // construct vector for phrase set with phrase j added                    
                if (sol != null) { 
                    newSol = sol.clone();
                    newSol.maxMerge(phraseVectors[j]);
                }
                else newSol = phraseVectors[j];
                assert(newSol != null); 
                assert(docVector != null);
                double sc = docVector.sumMinShared(newSol);
                if (sc > optSol) {
                    optSol = sc;
                    optPhrase = j;
                }
            }
            }            
            assert(optPhrase != -1);
            // add new phrase to phrase set
            optPhraseSet.add(optPhrase);
            // aggregate new phrase vector to phrase set
            if (sol == null) {
                sol = phraseVectors[optPhrase].clone();
            }
            else {
                sol.maxMerge(phraseVectors[optPhrase]);
            }
        }
        List<Phrase> result = new ArrayList<Phrase>();
        for (int ph : optPhraseSet) result.add(docPhrases.get(ph));    
        return result;
    }

    // extract phrases and construct phrase vectors
    private void processPhrases() throws Exception {
        phExtr = new PosRegexPhraseExtractor(config);
        docPhrases = phExtr.extractPhrases(doc.getText());
        phraseVectors = new IRealVector[docPhrases.size()];
        VectorAggregator agg = new VectorAggregator(wordVec);
        int i = 0;
        for (Phrase ph : docPhrases) {
            phraseVectors[i++] = agg.aggregate(ph.getCanonicTokens(), 
                    VectorAggregator.Method.MAX);
        }
    }

}
