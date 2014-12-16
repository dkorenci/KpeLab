package hr.irb.zel.kpelab.vectors.phrase;

import hr.irb.zel.kpelab.phrase.Phrase;
import vectors.IRealVector;

public interface IPhraseVectorizer {
    public IRealVector phraseToVector(Phrase phrase) throws Exception;
}
