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

public class DayShiftStartInfoHelper
{
    private static final Logger logger = (Logger) LoggerFactory.getLogger(DayShiftStartInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static String dayShiftStart = "";
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.DAY_SHIFT_START;

    private DayShiftStartInfoHelper()
    {
    }

    public static DayShiftStartInfoHelper getInstance()
    {
        return DayShiftStartInfoHelper.Holder.INSTANCE;
    }

    public String getDayShiftStart()
    {
        synchronized (lock)
        {
            if (updated)
                return dayShiftStart;

            try
            {
                dayShiftStart = ((String) options.getPreference(preferenceName, String.class));
            }
            catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected)
            {
                logger.error("DayShiftStartInfoHelper:  dayShiftStart error! " +
                        "Could not getPreference from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                dayShiftStart = "";
                logger.error("DayShiftStartInfoHelper:  dayShiftStart error! " +
                        "Assigned empty string to start time due to emergency mode!");
            }
            updated = true;
            return dayShiftStart;
        }
    }

    public void setDayShiftStart(String newShiftStart) throws IOException
    {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newShiftStart);

        synchronized (lock)
        {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("Day shift start has been set to <{}>", newShiftStart);
        Connector.updateSystem(null);

    }

    public static class Holder
    {
        static final DayShiftStartInfoHelper INSTANCE = new DayShiftStartInfoHelper();
    }
}
