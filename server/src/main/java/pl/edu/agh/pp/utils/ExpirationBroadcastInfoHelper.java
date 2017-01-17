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

public class ExpirationBroadcastInfoHelper {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ExpirationBroadcastInfoHelper.class);
    private static final Object lock = new Object();
    private static IOptions options = Options.getInstance();
    private static int expirationBroadcast = 0;
    private static boolean updated = false;
    private static String preferenceName = PreferencesNamesHolder.ANOMALY_EXPIRATION_BROADCAST;

    private ExpirationBroadcastInfoHelper() {
    }

    public static ExpirationBroadcastInfoHelper getInstance() {
        return ExpirationBroadcastInfoHelper.Holder.INSTANCE;
    }

    public int getExpirationBroadcastValue() {
        synchronized (lock) {
            if (updated)
                return expirationBroadcast;

            try {
                expirationBroadcast = ((int) options.getPreference(preferenceName, Integer.class));
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("ExpirationBroadcastInfoHelper:  expirationBroadcast error! " +
                                "Could not getPreference AnomalyExpirationBroadcast from registry!" + illegalPreferenceObjectExpected,
                        illegalPreferenceObjectExpected);
                expirationBroadcast = 0;
                logger.error("ExpirationBroadcastInfoHelper:  expirationBroadcast error! " +
                        "Assigned 0 secs to broadcast time due to emergency mode!");
            }
            updated = true;
            return expirationBroadcast;
        }
    }

    public void setExpirationBroadcastValue(int newExpirationBroadcastValue) throws IOException {

        HashMap<String, Object> newPreference = new HashMap<>();
        newPreference.put(preferenceName, newExpirationBroadcastValue);

        synchronized (lock) {
            updated = false;
            options.setPreferences(newPreference);
        }
        logger.info("Expiration broadcast has been set to <{}>", newExpirationBroadcastValue);
        Connector.updateSystem(null);

    }

    public static class Holder {
        static final ExpirationBroadcastInfoHelper INSTANCE = new ExpirationBroadcastInfoHelper();
    }
}
