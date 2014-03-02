package hr.irb.zel.kpelab.extraction;

import hr.irb.zel.kpelab.corpus.KpeDocument;
import hr.irb.zel.kpelab.phrase.Phrase;
import hr.irb.zel.kpelab.util.IComponent;
import java.util.List;

public interface IKpextractor extends IComponent {

    public List<Phrase> extract(KpeDocument doc) throws Exception;
    
}
