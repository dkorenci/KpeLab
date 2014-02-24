package hr.irb.zel.kpelab.analysis.devel;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.vectors.IRealVector;
import hr.irb.zel.kpelab.vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test of processing components on development set.
 */
public class DevelTester {    
    
    private final GreedyExtractorConfig c;
    // maps document id to mapping of phrase set names to sets
    Map< String, Map<String, List<Phrase>> > develSet;
    Map< String, String > docTexts;
    private final String [] phraseSets = {"bad","semi","gold"};
    private BufferedWriter writer;
    private final String outFolder;
 
    private class TestInstance implements Comparable<TestInstance> {
        public String phSetId;
        public double result; // result of vector similarity comparison
        public double expectedRank; // expected rank of the phrase set
        
        public TestInstance(String id, double r, double e) {
            phSetId = id; result = r; expectedRank = e;                    
        }

        public int compareTo(TestInstance i) {
            int cmp = Double.compare(result, i.result);
            if (cmp != 0) return -cmp;
            else return phSetId.compareTo(i.phSetId);
        }
                
    }
    
    public DevelTester(GreedyExtractorConfig conf) {
        c = conf;        
        outFolder = KpeConfig.getProperty("devel.tests");
    }
    
    public void testPhraseSetOrder() throws IOException, Exception {
        if (develSet == null) readDevelSet(); 
        if (writer == null) writer = new BufferedWriter(new FileWriter(outFolder+"develTests.txt"));
        
        List<TestInstance> instances = getBasicInstances();
        for (String docId : develSet.keySet()) {
            IRealVector docVec = c.docVectorizer.vectorize(docTexts.get(docId));
            for (TestInstance inst : instances) {
                List<Phrase> phr = develSet.get(docId).get(inst.phSetId);
                IRealVector phVec = createPhraseVector(phr);
                inst.result = c.phraseSetQuality.compare(phVec, docVec);                
            }
            writer.write("*"+docId+"\n");            
            //sortByRank(instances);
            sortByResult(instances);
            writer.write("sorted by quality: ");
            for (TestInstance inst : instances) {
                writer.write(inst.phSetId+":"+inst.result+" ");
            }
            writer.write("\n");
        }
        writer.close();
    }

    private void sortByRank(List<TestInstance> instances) {
        Collections.sort(instances, new Comparator<TestInstance>() {
            public int compare(TestInstance o1, TestInstance o2) {
                return -Double.compare(o1.expectedRank, o2.expectedRank);
            }           
        });
    }

    private void sortByResult(List<TestInstance> instances) {
        Collections.sort(instances);
    }    
    
    // create phrase vector using config phrase vectorizer
    private IRealVector createPhraseVector(List<Phrase> phrases) throws Exception {
        for (Phrase ph : phrases) c.phVectorizer.addPhrase(ph);
        return c.phVectorizer.vector();
    }    
    
    // get list of test instances for basic benchmark sets from phraseSets[]
    private List<TestInstance> getBasicInstances() {
        List<TestInstance> instances = new ArrayList<TestInstance>();
        int e = 1;
        for (String phSetId : phraseSets) {
            instances.add(new TestInstance(phSetId, 0, e++));
        }
        return instances;
    }
    
    private void readDevelSet() throws IOException {
        List<KpeDocument> docs = CorpusSemeval.getDataset("devel", SolutionPhraseSet.AUTHOR);
        System.out.println(docs.size());
        develSet = new TreeMap<String, Map<String, List<Phrase>>>();
        docTexts = new TreeMap<String, String>();
        for (KpeDocument d : docs) {
            String id = d.getId();
            Map<String, List<Phrase>> m = new TreeMap<String, List<Phrase>>();
            for (String phSetId : phraseSets) {
                KpeDocument tmp = CorpusSemeval.getDocument("devel/"+id, phSetId);
                m.put(phSetId, tmp.getKeyphrases());
            }
            develSet.put(id, m);
            docTexts.put(id, d.getText());
        }
    }
    
}
