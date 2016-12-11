package pl.edu.agh.pp.utils;

import ch.qos.logback.classic.Logger;
import com.google.maps.GeoApiContext;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.Options;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.BackingStoreException;

/**
 * Created by Maciej on 14.05.2016.
 * 23:49
 * Project: 1.
 */

public class ContextLoader {

    private static final String API_KEY_PREFERENCES_NAME = "API_KEY";
    private Properties properties;
    private String propertiesFileName = "/config.properties";
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    private static List<GeoApiContext> contextList = new ArrayList<>();

    public GeoApiContext geoApiContextLoader() throws IOException, IllegalPreferenceObjectExpected {
        Options options = Options.getInstance();
        String apiKey = (String)options.getPreference("API_KEY", String.class);

        GeoApiContext context = new GeoApiContext().setApiKey(apiKey);
        contextList.add(context);
        return context;
    }

    @Deprecated
    private Properties loadAppProperties() throws IOException {
        logger.info("<" + this.getClass().getCanonicalName() + "> Loading App properties");

        Properties properties = new Properties();
        InputStream configStream = this.getClass().getResourceAsStream(propertiesFileName);

        properties.load(configStream);

        return properties;
    }

    public static void changeApiKey(String newApiKey) throws BackingStoreException, IllegalPreferenceObjectExpected {
        updatePreferences(newApiKey);
        for (GeoApiContext context : contextList) {
            context.setApiKey(newApiKey);
        }
    }

    private static void updatePreferences(String newApiKey) throws BackingStoreException, IllegalPreferenceObjectExpected {
        Options options = Options.getInstance();
        HashMap<String, Object> currentOptions = options.getPreferences();
        currentOptions.entrySet().stream().filter(entry -> Objects.equals(entry.getKey(), API_KEY_PREFERENCES_NAME))
                .forEach(entry -> entry.setValue(newApiKey));

        options.setPreferences(currentOptions);
        System.out.println("Api key changed to: " + options.getPreference("API_KEY", String.class));
    }
}
