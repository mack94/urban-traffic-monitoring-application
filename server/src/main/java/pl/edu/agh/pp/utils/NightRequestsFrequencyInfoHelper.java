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

public class NightRequestsFrequencyInfoHelper {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(NightRequestsFrequencyInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static int minimalTime = 0;
    private static int maximalTime = 0;
    private static boolean updatedMin = false;
    private static boolean updatedMax = false;
    private static String minPreferenceName = PreferencesNamesHolder.NIGHT_SHIFT_FREQUENCY_FROM;
    private static String maxPreferenceName = PreferencesNamesHolder.NIGHT_SHIFT_FREQUENCY_TO;

    private NightRequestsFrequencyInfoHelper() {
    }

    public static NightRequestsFrequencyInfoHelper getInstance() {
        return NightRequestsFrequencyInfoHelper.Holder.INSTANCE;
    }

    public int getMinimalTimeValue() {
        synchronized (lock) {
            if (updatedMin)
                return minimalTime;

            try {
                minimalTime = ((int) options.getPreference(minPreferenceName, Integer.class));
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("NightRequestsFrequencyInfoHelper:  minimalTime error! " +
                                "Could not getPreference from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                minimalTime = 0;
                logger.error("NightRequestsFrequencyInfoHelper:  minimalTime error! " +
                        "Assigned 0 secs to minimal time due to emergency mode!");
            }
            updatedMin = true;
            return minimalTime;
        }
    }

    public int getMaximalTimeValue() {
        synchronized (lock) {
            if (updatedMax)
                return maximalTime;

            try {
                maximalTime = ((int) options.getPreference(maxPreferenceName, Integer.class));
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("NightRequestsFrequencyInfoHelper:  maximalTime error! " +
                                "Could not getPreference from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                maximalTime = 0;
                logger.error("NightRequestsFrequencyInfoHelper:  maximalTime error! " +
                        "Assigned 0 secs to maximal time due to emergency mode!");
            }
            updatedMax = true;
            return maximalTime;
        }
    }

    public void setFrequenciesBounds(int newMinimalTime, int newMaximalTime) throws IOException {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(minPreferenceName, newMinimalTime);
        newPreference.put(maxPreferenceName, newMaximalTime);

        synchronized (lock) {
            updatedMin = false;
            updatedMax = false;
            options.setPreferences(newPreference);
        }
        logger.info("Night minimal time has been set to <{}>", newMinimalTime);
        logger.info("Night maximal time has been set to <{}>", newMaximalTime);
        Connector.updateSystem(null);

    }

    public static class Holder {
        static final NightRequestsFrequencyInfoHelper INSTANCE = new NightRequestsFrequencyInfoHelper();
    }
}
