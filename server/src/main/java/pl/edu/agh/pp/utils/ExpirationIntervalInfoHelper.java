/* Copyright 2016 Sabre Holdings */
package pl.edu.agh.pp.utils;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

public class ExpirationIntervalInfoHelper
{
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ExpirationIntervalInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static int expirationInterval = 0;
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.ANOMALY_EXPIRATION_INTERVAL;

    private ExpirationIntervalInfoHelper()
    {
    }

    public static ExpirationIntervalInfoHelper getInstance()
    {
        return ExpirationIntervalInfoHelper.Holder.INSTANCE;
    }

    public int getExpirationIntervalValue()
    {
        synchronized (lock)
        {
            if (updated)
                return expirationInterval;

            try
            {
                expirationInterval = ((int) options.getPreference(preferenceName, Integer.class));
            }
            catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected)
            {
                logger.error("ExpirationIntervalInfoHelper:  expirationInterval error! " +
                        "Could not getPreference AnomalyExpirationInterval from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                expirationInterval = 0;
                logger.error("ExpirationIntervalInfoHelper:  expirationInterval error! " +
                        "Assigned 0 secs to repeater interval due to emergency mode!");
            }
            updated = true;
            return expirationInterval;
        }
    }

    public void setExpirationIntervalValue(int newExpirationIntervalValue) throws IOException
    {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newExpirationIntervalValue);

        synchronized (lock)
        {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("Expiration interval has been set to <{}>", newExpirationIntervalValue);
        Connector.updateSystem(null);

    }

    public static class Holder
    {
        static final ExpirationIntervalInfoHelper INSTANCE = new ExpirationIntervalInfoHelper();
    }
}
