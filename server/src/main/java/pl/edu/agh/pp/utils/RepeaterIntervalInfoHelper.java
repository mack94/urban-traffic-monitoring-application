/* Copyright 2016 Sabre Holdings */
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

public class RepeaterIntervalInfoHelper
{
    private static final Logger logger = (Logger) LoggerFactory.getLogger(RepeaterIntervalInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static int repeaterInterval = 0;
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.ANOMALY_REPEATER_INTERVAL;

    private RepeaterIntervalInfoHelper() {
    }

    public static RepeaterIntervalInfoHelper getInstance() {
        return RepeaterIntervalInfoHelper.Holder.INSTANCE;
    }

    public int getRepeaterIntervalValue() {
        synchronized (lock) {
            if (updated)
                return repeaterInterval;

            try {
                repeaterInterval = ((int) options.getPreference(preferenceName, Integer.class));
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("RepeaterIntervalInfoHelper:  repeaterInterval error! " +
                                "Could not getPreference AnomalyRepeaterInterval from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                repeaterInterval = 0;
                logger.error("RepeaterIntervalInfoHelper:  repeaterInterval error! " +
                        "Assigned 0 secs to repeater interval due to emergency mode!");
            }
            updated = true;
            return repeaterInterval;
        }
    }

    public void setRepeaterIntervalValue(int newRepeaterIntervalValue) throws IOException
    {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newRepeaterIntervalValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("Anomaly repeater interval has been set to <{}>", newRepeaterIntervalValue);
        Connector.updateSystem(null);

    }

    public static class Holder {
        static final RepeaterIntervalInfoHelper INSTANCE = new RepeaterIntervalInfoHelper();
    }
}
