/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.irb.zel.kpelab.util;

// wrapper stream for serialization and deserialization of obje
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ObjectIO {

    private FSTObjectInput objectReader;
    private FSTObjectOutput objectWriter;
//        private ObjectInputStream objectReader;
//        private ObjectOutputStream objectWriter; 

    /**
     * @param serialize If true open serialization stream, else open
     * deserialization stream.
     */
    public ObjectIO(File file, boolean serialize) throws IOException {
        if (serialize) objectWriter = new FSTObjectOutput(new FileOutputStream(file));
        else objectReader = new FSTObjectInput(new FileInputStream(file));        
//            if (serialize) objectWriter = new ObjectOutputStream(new FileOutputStream(file));
//            else objectReader = new ObjectInputStream(new FileInputStream(file));               
    }

    public Object readObject() throws ClassNotFoundException, IOException {
        return objectReader.readObject();
    }

    public void writeObject(Object o) throws IOException {
        objectWriter.writeObject(o);
    }

    public void close() throws IOException {
        if (objectReader != null) {
            objectReader.close();
        }
        if (objectWriter != null) {
            objectWriter.close();
        }
    }
}