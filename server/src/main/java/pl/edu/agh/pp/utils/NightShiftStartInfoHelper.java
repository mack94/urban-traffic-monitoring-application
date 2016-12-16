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

public class NightShiftStartInfoHelper
{
    private static final Logger logger = (Logger) LoggerFactory.getLogger(NightShiftStartInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static String nightShiftStart = "";
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.NIGHT_SHIFT_START;

    private NightShiftStartInfoHelper()
    {
    }

    public static NightShiftStartInfoHelper getInstance()
    {
        return NightShiftStartInfoHelper.Holder.INSTANCE;
    }

    public String getNightShiftStart()
    {
        synchronized (lock)
        {
            if (updated)
                return nightShiftStart;

            try
            {
                nightShiftStart = ((String) options.getPreference(preferenceName, String.class));
            }
            catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected)
            {
                logger.error("NightShiftStartInfoHelper:  nightShiftStart error! " +
                        "Could not getPreference from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                nightShiftStart = "";
                logger.error("NightShiftStartInfoHelper:  nightShiftStart error! " +
                        "Assigned empty string to start time due to emergency mode!");
            }
            updated = true;
            return nightShiftStart;
        }
    }

    public void setNightShiftStart(String newShiftStart) throws IOException
    {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newShiftStart);

        synchronized (lock)
        {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("Night shift start has been set to <{}>", newShiftStart);
        Connector.updateSystem(null);

    }

    public static class Holder
    {
        static final NightShiftStartInfoHelper INSTANCE = new NightShiftStartInfoHelper();
    }
}
