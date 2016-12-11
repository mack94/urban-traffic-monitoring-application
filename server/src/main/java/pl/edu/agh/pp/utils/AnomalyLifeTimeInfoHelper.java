package pl.edu.agh.pp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Maciej on 14.11.2016.
 * 17:14
 * Project: server.
 */
public class AnomalyLifeTimeInfoHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(AnomalyLifeTimeInfoHelper.class);
    private static IOptions options = Options.getInstance();
    private static int anomalyLifeTime = 0;
    private static boolean updated = false;
    private static final Object lock = new Object();
    private static String preferenceName = "AnomalyLifeTime";

    private AnomalyLifeTimeInfoHelper() {
    }

    public static AnomalyLifeTimeInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    public int getAnomalyLifeTimeValue() {
        synchronized (lock) {
            if (updated)
                return anomalyLifeTime;

            try {
                anomalyLifeTime = (int) options.getPreference(preferenceName, Integer.class);
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("AnomalyLifeTimeInfoHelper:  anomalyLifeTime error! " +
                                "Could not getPreference AnomalyLifeTimeValue from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                anomalyLifeTime = 0;
                logger.error("AnomalyLifeTimeInfoHelper:  anomalyLifeTime error! " +
                        "Assigned 0 seconds to AnomalyLifeTimeValue due to emergency mode!");
            }
            updated = true;
            return anomalyLifeTime;
        }
    }

    public void setAnomalyLifeTimeValue(int newAnomalyLifeTimeValue) {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newAnomalyLifeTimeValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }

        try {
            Connector.updateSystem(null);
        } catch (IOException e) {
            logger.error("AnomalyLifeTimeInfoHelper: IOException error while setting " +
                    "anomaly life time value: " + e, e);
        } catch (IllegalPreferenceObjectExpected e) {
            logger.error("AnomalyLifeTimeInfoHelper: IllegalPreferenceObjectExpected error while setting " +
                    "anomaly life time value: " + e, e);
        }

    }

    public static class Holder {
        static final AnomalyLifeTimeInfoHelper INSTANCE = new AnomalyLifeTimeInfoHelper();
    }
}