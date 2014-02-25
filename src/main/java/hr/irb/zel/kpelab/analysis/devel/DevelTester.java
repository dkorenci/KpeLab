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

    // test instance with relevant data
    private class TestInstance implements Comparable<TestInstance> {
        public String phSetId;
        public double result; // result of vector similarity comparison
        public double expectedRank; // expected rank of the phrase set
        List<Phrase> phrases;
        
        public TestInstance(String id, double e, List<Phrase> ph) {
            phSetId = id; expectedRank = e; phrases = ph;
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
    
    public void testPhraseSets(String instanceSet) throws IOException, Exception {
        if (develSet == null) readDevelSet(); 
        if (writer == null) writer = new BufferedWriter(
                new FileWriter(outFolder+"_develTests_"+instanceSet+".txt"));
                
        for (String docId : develSet.keySet()) {
            List<TestInstance> instances = getInstances(instanceSet, docId);
            IRealVector docVec = c.docVectorizer.vectorize(docTexts.get(docId));
            for (TestInstance inst : instances) {                
                IRealVector phVec = createPhraseVector(inst.phrases);
                if (phVec == null) System.out.println(phrasesToString(inst.phrases));
                inst.result = c.phraseSetQuality.compare(phVec, docVec);                
            }
            writer.write("*"+docId+"\n");            
            //sortByRank(instances);
            sortByResult(instances);                       
            for (TestInstance inst : instances) {
                writer.write(inst.phSetId+": "+inst.result+" ; "
                        +phrasesToString(inst.phrases)+"\n");
            }
            writer.write("\n");
        }
        writer.close();
    }
        
    private String phrasesToString(List<Phrase> phrases) {
        String str = "";
        for (Phrase ph : phrases) str += ph + ";";
        return str;
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
        c.phVectorizer.clear();
        for (Phrase ph : phrases) c.phVectorizer.addPhrase(ph);
        return c.phVectorizer.vector();
    }    

    private List<TestInstance> getInstances(String instanceSet, String docId) {
        if (instanceSet.equals("basic")) return getBasicInstances(docId);
        else if (instanceSet.equals("mixed")) return getMixedInstances(docId);
        else if (instanceSet.equals("single")) return getSinglePhraseInstances(docId);
        else return null;
    }    

    // for each set, get all individual phrases as instances
    private List<TestInstance> getSinglePhraseInstances(String docId) {
        List<TestInstance> instances = new ArrayList<TestInstance>();
        String [] phSets = {"gold", "semi", "bad"}; // sets ordered by quality
        for (int i = 0, r = 3; i < phSets.length; ++i, --r) {
            List<Phrase> phrases = develSet.get(docId).get(phSets[i]);
            for (int j = 0; j < phrases.size(); ++j) {
                instances.add(new TestInstance(phSets[i]+""+j, r, 
                        phrases.subList(j, j+1)));
            }
        }
        return instances;
    }    
    
    // instances are phrase sets than contain N-k phrases form higher 
    // quality set, and k phrases from lower quality set, where N is 
    // number of phrases in all sets, and k = 1..N-1
    // this way higher quality phrases are gradually replaced by lower quality ones
    private List<TestInstance> getMixedInstances(String docId) {
        List<TestInstance> instances = new ArrayList<TestInstance>();
        String [] phSets = {"gold", "semi", "bad"}; // sets ordered by quality
        int rank = 100;
        for (int i = 0; i < phSets.length-1; ++i) {            
            List<Phrase> higherQ = develSet.get(docId).get(phSets[i]);
            List<Phrase> lowerQ = develSet.get(docId).get(phSets[i+1]);
            int N = higherQ.size();
            assert(N == lowerQ.size());
            instances.add(new TestInstance(phSets[i], rank--, higherQ));
            // add sets by gradually removing phrases from phsets[i] and adding to phsets[i+1]
            for (int j = 1; j <= N-1; ++j) {
                List<Phrase> mixedQ = 
                    new ArrayList<Phrase>(higherQ.subList(0, N-j));
                mixedQ.addAll(lowerQ.subList(0, j));
                instances.add(new TestInstance(phSets[i]+"-"+j, rank--, mixedQ));
            }
        }
        // add worst set
        instances.add(new TestInstance(phSets[phSets.length-1], rank--, 
                develSet.get(docId).get(phSets[phSets.length-1])));
        return instances;
    }    
    
    // get list of test instances for basic benchmark sets from phraseSets[]
    private List<TestInstance> getBasicInstances(String docId) {
        List<TestInstance> instances = new ArrayList<TestInstance>();
        int e = 1;
        for (String phSetId : phraseSets) {            
            instances.add(
             new TestInstance(phSetId, e++, develSet.get(docId).get(phSetId)));
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
