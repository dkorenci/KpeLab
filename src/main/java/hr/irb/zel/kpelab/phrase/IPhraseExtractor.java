package hr.irb.zel.kpelab.phrase;

import hr.irb.zel.kpelab.util.IComponent;
import java.util.List;

public interface IPhraseExtractor extends IComponent {
    public List<Phrase> extractPhrases(String text) throws Exception;
}
