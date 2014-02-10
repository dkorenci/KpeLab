package hr.irb.zel.kpelab.phrase;

import java.util.List;

public interface IPhraseExtractor {
    public List<Phrase> extractPhrases(String text) throws Exception;
}
