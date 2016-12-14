package pl.edu.agh.pp.utils;

import com.google.maps.GeoApiContext;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

/**
 * Created by Maciej on 14.05.2016.
 * 23:49
 * Project: 1.
 */

public class ContextLoader {

    private static final String preferenceName = PreferencesNamesHolder.DETECTOR_API_KEY;
    private static List<GeoApiContext> contextList = new ArrayList<>();

    public static void changeApiKey(String newApiKey) throws BackingStoreException, IllegalPreferenceObjectExpected {
        for (GeoApiContext context : contextList) {
            context.setApiKey(newApiKey);
        }
    }

    public GeoApiContext geoApiContextLoader() throws IOException, IllegalPreferenceObjectExpected {
        Options options = Options.getInstance();
        String apiKey = (String) options.getPreference(preferenceName, String.class);

        GeoApiContext context = new GeoApiContext().setApiKey(apiKey);
        contextList.add(context);
        return context;
    }
}
