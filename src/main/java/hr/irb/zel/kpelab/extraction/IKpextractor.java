package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import java.util.List;

public interface IKpextractor {

    public List<Phrase> extract(KpeDocument doc) throws Exception;
    
}
