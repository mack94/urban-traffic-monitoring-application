package pl.edu.agh.pp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;

import java.util.HashMap;

/**
 * Created by Maciej on 09.11.2016.
 * 21:52
 * Project: server.
 */
public class LeverInfoHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(LeverInfoHelper.class);
    private static IOptions options = Options.getInstance();
    private static double leverValue = 0.0;
    private static boolean updated = false;
    private static final Object lock = new Object();
    private static String preferenceName = "LeverValue";

    private LeverInfoHelper() {
    }

    public static LeverInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    public double getLeverValue() {
        synchronized (lock) {
            if (updated)
                return leverValue;

            try {
                leverValue = ((int) options.getPreference(preferenceName, Integer.class)) / 100.0;
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("LeverInfoHelper:  leverValue error! Could not getPreference LeverValue from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                leverValue = 0.0;
                logger.error("LeverInfoHelper:  leverValue error! Assigned 0.0 (0%) to LeverValue due to emergency mode!");
            }
            updated = true;
            return leverValue;
        }
    }

    public void setLeverValue(int newLeverValue) {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newLeverValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }
        Connector.updateLever(getLeverValue());

    }

    public static class Holder {
        static final LeverInfoHelper INSTANCE = new LeverInfoHelper();
    }

}
