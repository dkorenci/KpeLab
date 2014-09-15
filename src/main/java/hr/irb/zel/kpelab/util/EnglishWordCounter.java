package hr.irb.zel.kpelab.util;

import hr.irb.zel.kpelab.phrase.PhraseHelper;

/**
 *
 */
public class EnglishWordCounter implements IWordCounter {

    public int countWords(String text) throws Exception {
        return PhraseHelper.countWords(text);
    }

}
