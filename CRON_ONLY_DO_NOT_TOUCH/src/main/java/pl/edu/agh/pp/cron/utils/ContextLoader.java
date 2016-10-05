package pl.edu.agh.pp.cron.utils;

import ch.qos.logback.classic.Logger;
import com.google.maps.GeoApiContext;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Maciej on 14.05.2016.
 * 23:49
 * Project: 1.
 */
public class ContextLoader {

    private Properties properties;
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

    public GeoApiContext geoApiContextLoader() throws IOException {
        properties = loadAppProperties();
        String apiKey = properties.getProperty("ApiKey");

        return new GeoApiContext().setApiKey(apiKey);
    }

    private Properties loadAppProperties() throws IOException {
        logger.info("<" + this.getClass().getCanonicalName() + "> Loading App properties");

        Properties properties = new Properties();
        InputStream configStream = this.getClass().getResourceAsStream("/config.properties");

        properties.load(configStream);

        return properties;
    }
}
