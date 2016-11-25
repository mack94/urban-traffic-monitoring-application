package pl.edu.agh.pp.utils;

import ch.qos.logback.classic.Logger;
import com.google.maps.GeoApiContext;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Maciej on 14.05.2016.
 * 23:49
 * Project: 1.
 */

public class ContextLoader {

    private Properties properties;
    private String propertiesFileName = "/config.properties";
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    private static List<GeoApiContext> contextList = new ArrayList<>();

    public GeoApiContext geoApiContextLoader() throws IOException {
        properties = loadAppProperties();
        String apiKey = properties.getProperty("ApiKey");

        GeoApiContext context = new GeoApiContext().setApiKey(apiKey);
        contextList.add(context);
        return context;
    }

    private Properties loadAppProperties() throws IOException {
        logger.info("<" + this.getClass().getCanonicalName() + "> Loading App properties");

        Properties properties = new Properties();
        InputStream configStream = this.getClass().getResourceAsStream(propertiesFileName);

        properties.load(configStream);

        return properties;
    }

    public static void changeApiKey(String newApiKey) {
        for (GeoApiContext context : contextList) {
            context.setApiKey(newApiKey);
        }
    }
}
