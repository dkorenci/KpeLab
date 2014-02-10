package hr.irb.zel.kpelab.config;

import java.io.FileInputStream;
import java.util.Properties;


/** Initializes and provides application configuration. */
public class KpeConfig {

    private static String propertiesFile = "config/kpelab.properties";
    
    //private static Configuration config;
    private static Properties props;
    
    private static Properties getConfig() {
        if (props == null) {
            try {                                                
                props = new Properties();
                props.load( new FileInputStream(propertiesFile) );                
            } catch (Exception e) {
                System.out.println("configuration initialization error:");
                System.out.println(e);
                System.out.println(e.getStackTrace());
                System.exit(1);
            }
        }
        return props;
    }
    
    public static String getProperty(String name) {
        return getConfig().getProperty(name);
    }
    
}
