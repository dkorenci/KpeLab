package hr.irb.zel.kpelab.corpus.semeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Read and process document text.
 */
public class DocumentReader {

    public static String readDocument(File doc) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(doc));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }
    
}
