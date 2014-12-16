package hr.irb.zel.kpelab.analysis.devel;

import hr.irb.zel.kpelab.config.KpeConfig;
import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.corpus.semeval.CorpusSemeval;
import hr.irb.zel.kpelab.corpus.semeval.SolutionPhraseSet;
import hr.irb.zel.kpelab.evaluation.F1Evaluator;
import hr.irb.zel.kpelab.evaluation.F1Metric;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality;
import hr.irb.zel.kpelab.evaluation.IPhraseEquality.PhEquality;
import hr.irb.zel.kpelab.evaluation.SemevalPhraseEquality;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractor;
import hr.irb.zel.kpelab.extraction.greedy.GreedyExtractorConfig;
import hr.irb.zel.kpelab.extraction.greedy.phrase.IPhraseSetVectorizer;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.util.REngineManager;
import hr.irb.zel.kpelab.util.Utils;
import vectors.IRealVector;
import vectors.comparison.IVectorComparison;
import hr.irb.zel.kpelab.vectors.document.IDocumentVectorizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.DelayQueue;
import javax.swing.plaf.metal.MetalIconFactory;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

/**
 * Test of processing components on development set.
 */
public class DevelTester {    
    
    private final GreedyExtractorConfig c;
    // maps document id to mapping of phrase set names to sets
    Map< String, Map<String, List<Phrase>> > develSet;
    Map< String, String > docTexts;
    private final String [] phraseSets = {"bad","semi","gold"};
    private BufferedWriter writer, summary, overview;
    private final String outFolder, develFolder;    
    
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
        outFolder = KpeConfig.getProperty("devel.tests")+c.getId()+"/";
        develFolder = KpeConfig.getProperty("devel.tests");
    }
    
    /** Init empty folder for results. */
    public void init() throws IOException {
        File rfolder = new File(outFolder);        
        if (!rfolder.exists()) rfolder.mkdir();  
        openOverview();
        openSummary();
    }

    public void close() throws IOException {       
        closeOverview();
        closeSummary();
    }    
    
    // overview is a log file i devel.tests folder for writing result summaries
    private void openOverview() throws IOException {
        String fname = "_overview.txt";
        overview = new BufferedWriter(new FileWriter(develFolder+fname, true));
        overview.write(c.getId()+"\n");
    }
    
    private void closeOverview() throws IOException {
        overview.write("\n");
        overview.close();
    }    
    
    
    private void openSummary() throws IOException {
        String fname = "summary."+c.getId()+".txt";
         summary = new BufferedWriter(new FileWriter(outFolder+fname));
    }
    
    private void closeSummary() throws IOException {
         summary.close();
    }    
    
    /** For each document in developement set, sort phrase sets by quality, 
     *  type of phrase set is set with instanceSet, can be: basic, mixed, single. */
    public void testPhraseSets(String instanceSet, int numDocs) throws IOException, Exception {
        if (develSet == null) readDevelSet(); 
        writer = new BufferedWriter(new FileWriter(outFolder+"_develTests_"+instanceSet+".txt"));
        int docCnt = 0;
        double spearmanAvg = 0;        
        for (String docId : develSet.keySet()) {
            System.out.println("testPhraseSets " + docId);
            c.adaptToDocument(docTexts.get(docId));
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
                writer.write(inst.phSetId+": "+Utils.doubleStr(inst.result)+" ; "
                        +phrasesToString(inst.phrases)+"\n");
            }
            double s = calculateSpearman(instances);
            spearmanAvg += s;
            writer.write("spearman: " + Utils.doubleStr(s) + "\n");
            writer.write("\n");
            if (++docCnt == numDocs) break;
        }        
        spearmanAvg /= numDocs;
        writer.close();
        
        summary.write("spearman avg. " + instanceSet + " : " 
                + Utils.doubleStr(spearmanAvg) + "\n");        
        
        overview.write(instanceSet + " : " + Utils.doubleStr(spearmanAvg) + " ");
    }

   // run extraction on a subset of train documents
    public void runOnSample(int sampleSize, int K) throws IOException, Exception {
        System.out.println("run on sample of size " + sampleSize);
        List<KpeDocument> docs = CorpusSemeval.getDataset("devel", SolutionPhraseSet.COBINED);
        if (sampleSize < 0 || sampleSize > docs.size()) sampleSize = docs.size();
        docs = docs.subList(0, sampleSize);
        writer = new BufferedWriter(new FileWriter(outFolder+"_develTests_docExtract.txt"));
        F1Metric result = new F1Metric();
        GreedyExtractor greedy = new GreedyExtractor(K, c);            
        F1Evaluator eval = new F1Evaluator(greedy, PhEquality.SEMEVAL);            
        for (KpeDocument doc : docs) {              
            System.out.println("processing " + doc.getId());
            F1Metric r = eval.evaluateDocument(doc);
            writer.write("*"+doc.getId()+"\n");
            writer.write("correct   phrases: " +  phrasesToString(doc.getKeyphrases())+"\n");
            writer.write("extracted phrases: " +  phrasesToString(r.phrases)+"\n");
            writer.write("result: " + r);
            writer.write("\n");
            result.add(r);
        }        
        result.divide(docs.size());
        writer.write("micro averaged result: " + result + "\n");
        writer.close();
        
        summary.write("micro averaged result: " + result + "\n");
        overview.write("\nmicro averaged result: " + result + "\n");
    }    

    // evalueate extractor on a subsample of size S of train
    public void runOnTrainSubsample(int K) 
            throws Exception {
        final int SAMPLE_SIZE = 20;
        final int SEED = 889123;
        List<KpeDocument> docs = CorpusSemeval.getDataset("train", SolutionPhraseSet.COBINED);
        List<KpeDocument> sample = Utils.getRandomSubsample(docs, SAMPLE_SIZE, SEED);        
        GreedyExtractor greedy = new GreedyExtractor(K, c);         
        F1Evaluator eval = new F1Evaluator(greedy, PhEquality.SEMEVAL);
        F1Metric metric = eval.evaluateDocuments(sample);
        summary.write("train subsample of size " + SAMPLE_SIZE + ": " + metric + "\n");
        overview.write("train subsample of size " + SAMPLE_SIZE + ": " + metric + "\n");                
    }      
    
    private String phrasesToString(List<Phrase> phrases) {
        String str = "";
        for (Phrase ph : phrases) str += ph + ";";
        return str;
    }    
    
    // calculcate spearman correlation between expected rank and calculated result
    // for test instances
    private double calculateSpearman(List<TestInstance> instances) throws Exception {
        Rengine rengine = REngineManager.getRengine();
        double [] exp = new double[instances.size()];
        double [] res = new double[instances.size()];
        int i = 0;
        for (TestInstance inst : instances) {
            exp[i] = inst.expectedRank; res[i++] = inst.result;
        }
        rengine.assign("e", exp);
        rengine.assign("r", res);
        REXP result = rengine.eval("(cor(e,r,method=\"spearman\"))");        
        return result.asDouble();
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
    
    private void readDevelSet() throws IOException, Exception {
        List<KpeDocument> docs = CorpusSemeval.getDataset("devel", SolutionPhraseSet.AUTHOR);
        System.out.println(docs.size());
        develSet = new TreeMap<String, Map<String, List<Phrase>>>();
        docTexts = new TreeMap<String, String>();
        SemevalPhraseEquality phEq = new SemevalPhraseEquality();
        for (KpeDocument d : docs) {
            List<Phrase> extractedPhrases = c.phraseExtractor.extractPhrases(d.getText());            
            String id = d.getId();
            Map<String, List<Phrase>> m = new TreeMap<String, List<Phrase>>();
            for (String phSetId : phraseSets) {
                KpeDocument tmp = CorpusSemeval.getDocument("devel/"+id, phSetId);
                List<Phrase> matched = new ArrayList<Phrase>(5);
                for (Phrase ph : tmp.getKeyphrases()) {
                    Phrase match = null;
                    for (Phrase eph : extractedPhrases) {
                        if (phEq.equal(eph, ph)) {
                            match = eph; 
                            break;
                        }
                    }
                    if (match == null) {
                        throw new RuntimeException("unmatched: " + id + phSetId +" : "+ ph);
                        //System.out.println("unmatched: " + id + phSetId +" : "+ ph);
                    }
                    else matched.add(match);
                }                
                m.put(phSetId, matched);
            }
            develSet.put(id, m);
            docTexts.put(id, d.getText());
        }
    }   
    
}
