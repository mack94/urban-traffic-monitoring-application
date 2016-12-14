package pl.edu.agh.pp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Maciej on 14.12.2016.
 * 00:23
 * Project: server.
 */
public class ApisHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ApisHelper.class);
    private static IOptions options = Options.getInstance();
    private static String mapsApiKey = "";
    private static String detectorApiKey = "";
    private static boolean updatedMaps = false;
    private static boolean updatedDetector = false;
    private static final Object lock = new Object();
    private static String mapsPreferenceName = PreferencesNamesHolder.MAPS_API_KEY;
    private static String detectorPreferenceName = PreferencesNamesHolder.DETECTOR_API_KEY;

    private ApisHelper()
    {
    }

    public static ApisHelper getInstance()
    {
        return Holder.INSTANCE;
    }

    public String getMapsApiKey()
    {
        synchronized (lock)
        {
            if (updatedMaps)
                return mapsApiKey;

            try
            {
                mapsApiKey = (String) options.getPreference(mapsPreferenceName, String.class);
            }
            catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected)
            {
                logger.error("ApisHelper:  mapsApiKey error! " +
                                "Could not getPreference mapsApiKeyValue from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                mapsApiKey = "";
                logger.error("ApisHelper:  mapsApiKey error! " +
                        "Assigned empty key to mapsApiKeyValue due to emergency mode!");
            }
            updatedMaps = true;
            return mapsApiKey;
        }
    }

    public void setMapsApiKey(String newMapsApiKey) throws IOException {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(mapsPreferenceName, newMapsApiKey);

        synchronized (lock)
        {
            updatedMaps = false;
            options.setPreferences(newPreference);
        }
        Connector.updateSystem(null);

    }

    public String getDetectorApiKey()
    {
        synchronized (lock)
        {
            if (updatedDetector)
                return detectorApiKey;

            try
            {
                detectorApiKey = (String) options.getPreference(detectorPreferenceName, String.class);
            }
            catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected)
            {
                logger.error("ApisHelper:  detectorApiKey error! " +
                                "Could not getPreference detectorApiKeyValue from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                detectorApiKey = "";
                logger.error("ApisHelper:  detectorApiKey error! " +
                        "Assigned empty key to detectorApiKeyValue due to emergency mode!");
            }
            updatedDetector = true;
            return detectorApiKey;
        }
    }

    public void setDetectorApiKey(String newDetectorApiKey) throws IOException {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(detectorPreferenceName, newDetectorApiKey);

        synchronized (lock)
        {
            updatedDetector = false;
            options.setPreferences(newPreference);
        }
        Connector.updateSystem(null);

    }

    public static class Holder
    {
        static final ApisHelper INSTANCE = new ApisHelper();
    }
}
