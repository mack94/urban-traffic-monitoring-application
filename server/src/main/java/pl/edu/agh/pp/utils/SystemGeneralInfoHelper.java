package pl.edu.agh.pp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

import java.util.HashMap;

/**
 * Created by Maciej on 27.12.2016.
 * 18:47
 * Project: server.
 */
public class SystemGeneralInfoHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(SystemGeneralInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static int anomalyChannelPort = 8080;
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.ANOMALY_CHANNEL_PORT;

    private SystemGeneralInfoHelper() {
    }

    public static SystemGeneralInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    public int getAnomalyChannelPort() {
        synchronized (lock) {
            if (updated)
                return anomalyChannelPort;

            try {
                anomalyChannelPort = (int) options.getPreference(preferenceName, Integer.class);
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("SystemGeneralInfoHelper:  anomaly channel port error! " +
                                "Could not getPreference AnomalyServerPort from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                anomalyChannelPort = 8080;
                setAnomalyChannelPort(anomalyChannelPort);
                logger.error("SystemGeneralInfoHelper:  anomaly channel port error! " +
                        "Assigned 8080 to AnomalyServerPort due to emergency mode!");
            }
            updated = true;
            return anomalyChannelPort;
        }
    }

    public void setAnomalyChannelPort(int newLeverValue) {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newLeverValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("SystemGeneralInfoHelper: Anomaly Server Port has been set to <{}>", newLeverValue);
        Connector.updateLever(getAnomalyChannelPort());

    }

    public static class Holder {
        static final SystemGeneralInfoHelper INSTANCE = new SystemGeneralInfoHelper();
    }

}