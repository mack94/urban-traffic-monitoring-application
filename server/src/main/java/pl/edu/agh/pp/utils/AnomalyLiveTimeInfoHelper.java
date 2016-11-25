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
public class AnomalyLiveTimeInfoHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(AnomalyLiveTimeInfoHelper.class);
    private static IOptions options = Options.getInstance();
    private static int anomalyLiveTime = 0;
    private static boolean updated = false;
    private static final Object lock = new Object();
    private static String preferenceName = "AnomalyLiveTime";

    private AnomalyLiveTimeInfoHelper() {
    }

    public static AnomalyLiveTimeInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    public int getAnomalyLiveTimeValue() {
        synchronized (lock) {
            if (updated)
                return anomalyLiveTime;

            try {
                anomalyLiveTime = (int) options.getPreference(preferenceName, Integer.class);
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("AnomalyLiveTimeInfoHelper:  anomalyLiveTime error! " +
                                "Could not getPreference AnomalyLiveTimeValue from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                anomalyLiveTime = 0;
                logger.error("AnomalyLiveTimeInfoHelper:  anomalyLiveTime error! " +
                        "Assigned 0 seconds to AnomalyLiveTimeValue due to emergency mode!");
            }
            updated = true;
            return anomalyLiveTime;
        }
    }

    public void setAnomalyLiveTimeValue(int newAnomalyLiveTimeValue) {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newAnomalyLiveTimeValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }

        try {
            Connector.updateSystem(null);
        } catch (IOException e) {
            logger.error("AnomalyLiveTimeInfoHelper: IOException error while setting " +
                    "anomaly live time value: " + e, e);
        } catch (IllegalPreferenceObjectExpected e) {
            logger.error("AnomalyLiveTimeInfoHelper: IllegalPreferenceObjectExpected error while setting " +
                    "anomaly live time value: " + e, e);
        }

    }

    public static class Holder {
        static final AnomalyLiveTimeInfoHelper INSTANCE = new AnomalyLiveTimeInfoHelper();
    }
}
