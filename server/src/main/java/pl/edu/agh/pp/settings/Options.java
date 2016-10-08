package pl.edu.agh.pp.settings;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;

/**
 * Created by Maciej on 08.10.2016.
 *
 * @author Maciej Makówka
 *         13:34
 *         Project: server.
 */

public class Options implements IOptions {

    private static Options instance = null;

    private static Configuration configuration = new Configuration();

    private final Logger logger = (Logger) LoggerFactory.getLogger(Options.class);

    private Options() {

    }

    public static Options getInstance() {
        if (instance == null)
            instance = new Options();

        return instance;
    }

    @Override
    public void initialize() throws BackingStoreException, IOException {
        configuration.initialize();
    }

    @Override
    public Object getPreference(String key, Class expectedObjectClass) throws IllegalPreferenceObjectExpected {
        return configuration.getPreference(key, expectedObjectClass);
    }

    @Override
    public HashMap<String, Object> getPreferences() throws BackingStoreException {
        return configuration.getPreferences();
    }

    @Override
    public boolean setPreferences(HashMap<String, Object> preferences) {
        return configuration.setPreferences(preferences);
    }

    @Override
    public boolean resetPreferences() throws BackingStoreException, IOException {
        configuration.resetPreferences();
        logger.info("Warning! Preferences have been reset!");
        return true;
    }

    @Override
    public boolean removePreferences(String key) {
        configuration.removePreference(key);
        return true;
    }

    @Override
    public byte[] getBytesPreferences() {
        return configuration.getBytesPreferences();
    }

    @Override
    public void setBytesPreferences(byte[] bytesPreferences) {
        configuration.setBytesPreferences(bytesPreferences);
    }

    @Override
    public String getFirstTruePreferenceFromPreferencesGroup(String key) {

        try {

            byte[] bytesPreferences = getBytesPreferences();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesPreferences);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            Map<String, HashMap<String, Boolean>> readHash;
            readHash = (Map<String, HashMap<String, Boolean>>) objectInputStream.readObject();

            HashMap<String, Boolean> values = readHash.get(key);

            for (String value : values.keySet())
                if (values.get(value))
                    return value;

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Options.getFirstTruePreferenceFromPreferencesGroup(): Error while getting group of Preference: "
                    + e.getMessage());
            return null;
        }

        logger.error("Options.getFirstTruePreferenceFromPreferencesGroup(): The method should have returned the result of query");
        return null;
    }

    @Override
    public boolean setOnlyTruePreferenceInPreferencesGroup(String group, String key) {

        try {

            byte[] bytesPreferences = getBytesPreferences();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesPreferences);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            Map<String, HashMap<String, Boolean>> readHash;
            readHash = (Map<String, HashMap<String, Boolean>>) objectInputStream.readObject();

            HashMap<String, Boolean> values = readHash.get(group);

            for (String value : values.keySet()) {
                if (value.compareTo(key) == 0)
                    values.replace(value, true);
                else
                    values.replace(value, false);
            }

            readHash.replace(group, values);

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);

            out.writeObject(readHash);
            setBytesPreferences(byteOut.toByteArray());

            return true;
        } catch (IOException e) {
            String msg = "Options.setOnlyTruePreferenceInPreferencesGroup(): IO Exception occurred. " +
                    "Probably while writing new object to the output stream to preferences. " + e;
            logger.error(msg);
            return false;
        } catch (ClassNotFoundException e) {
            String msg = "Options.setOnlyTruePreferenceInPreferencesGroup(): ClassNotFoundException occurred. " +
                    "Probably while reading stream object from preferences to Map readHash. " + e;
            logger.error(msg);
            return false;
        }
    }
}
