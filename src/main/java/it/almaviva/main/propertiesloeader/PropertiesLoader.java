package it.almaviva.main.propertiesloeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final String PROPERTIES_FILE = "config.properties";

    public Properties loadLocalProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }
}
