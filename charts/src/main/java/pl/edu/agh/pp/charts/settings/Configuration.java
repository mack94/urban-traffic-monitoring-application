package pl.edu.agh.pp.charts.settings;

import ch.qos.logback.classic.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Maciej on 08.10.2016.
 * 16:29
 * Project: charts.
 */

public class Configuration {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Configuration.class);
    private Preferences preferences = Preferences.userRoot().node(this.getClass().getName());

    public Configuration() {

    }

    public void printPreferences() throws BackingStoreException {
        HashMap<String, Object> allPreferences = getPreferences();

        for (String key : allPreferences.keySet()) {
            logger.info(key + " value= " + allPreferences.get(key));
        }
    }

    protected synchronized void initialize() throws BackingStoreException, IOException {
        if (!preferences.getBoolean("UTMAClientInitialized", false)) {
            resetPreferences();
            preferences.putBoolean("UTMAClientInitialized", true);
            logger.info("First UTMA client INITIALIZATION! Preferences have been set up");
        }
    }

    protected void resetPreferences() throws BackingStoreException, IOException {

        preferences.clear();
        logger.info("Preferences have been reset!");

        JSONArray jsonArray = PreferencesLoader.getInstance().loadJSON();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        HashMap<String, Boolean> hashValues = new HashMap<>();
        HashMap<String, Map<String, Boolean>> hashToWrite = new HashMap<>();

        int objects = jsonArray.length();

        for (int i = 0; i < objects; i++) {

            String name = jsonArray.getJSONObject(i).get("name").toString();
            String type = jsonArray.getJSONObject(i).get("type").toString();
            String value = jsonArray.getJSONObject(i).get("value").toString();

            switch (type) {
                case "Boolean":
                    setPreference(name, Boolean.valueOf(value));
                    break;
                case "Integer":
                    setPreference(name, Integer.valueOf(value));
                    break;
                case "String":
                    setPreference(name, value);
                    break;
                case "Stream":
                    JSONObject byteObject = jsonArray.getJSONObject(i).getJSONObject("value");
                    for (String key : byteObject.keySet()) {
                        hashValues.put(key, byteObject.getBoolean(key));
                    }

                    hashToWrite.put(name, hashValues);
                    break;
            }
        }

        if (hashToWrite.size() > 0) {
            objectOutputStream.writeObject(hashToWrite);

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            objectOutputStream.flush();
            objectOutputStream.close();
            Options.getInstance().setBytesPreferences(byteArrayOutputStream.toByteArray());

            logger.info("Configuration.resetPreferences(): Default preferences set.");
        }
    }

    protected HashMap<String, Object> getPreferences() throws BackingStoreException {

        String[] keys = getPreferencesKeys();

        return parseKeys(keys);
    }

    protected boolean setPreferences(HashMap<String, Object> preferences) {

        for (String key : preferences.keySet()) {
            Object value = preferences.get(key);

            setPreference(key, value);
        }

        return true;
    }

    protected Object getPreference(String key, Class expectedObjectClass) throws IllegalPreferenceObjectExpected {

        final Class<?> objectClass = expectedObjectClass;
        String keyToGetPreference = String.format("%s %s", expectedObjectClass.getSimpleName(), key);

        if (objectClass.isAssignableFrom(Boolean.class))
            return preferences.getBoolean(keyToGetPreference, false);
        else if (objectClass.isAssignableFrom(Integer.class))
            return preferences.getInt(keyToGetPreference, 0);
        else if (objectClass.isAssignableFrom(String.class))
            return preferences.get(keyToGetPreference, "");
        else if (objectClass.isAssignableFrom(byte[].class))
            return preferences.getByteArray(keyToGetPreference, new byte[]{});
        else
            throw new IllegalPreferenceObjectExpected();
    }

    protected void removePreference(String key) {
        preferences.remove(key);
    }

    private void setPreference(String optionName, Object value) {

        if (value instanceof Boolean)
            preferences.putBoolean("Boolean ".concat(optionName), (boolean) value);
        else if (value instanceof String)
            preferences.put("String ".concat(optionName), String.valueOf(value));
        else if (value instanceof Integer)
            preferences.putInt("Integer ".concat(optionName), (int) value);
        else if (value instanceof byte[]) {
            preferences.putByteArray("Stream ".concat(optionName), (byte[]) value);
        }
    }

    private String[] getPreferencesKeys() throws BackingStoreException {
        return preferences.keys();
    }

    private LinkedHashMap<String, Object> parseKeys(String[] keys) {

        LinkedHashMap<String, Object> parsedKeys = new LinkedHashMap<>();

        for (String key : keys) {
            String[] keyAttributes = key.split(" ");

            if (keyAttributes.length != 2)
                continue;

            Object keyValue = null;
            String keyType = keyAttributes[0];
            String keyName = keyAttributes[1];

            if (keyType.compareTo("Boolean") == 0)
                keyValue = preferences.getBoolean(key, false);
            else if (keyType.compareTo("String") == 0)
                keyValue = preferences.get(key, "");
            else if (keyType.compareTo("Integer") == 0)
                keyValue = preferences.getInt(key, 0);
            else if (keyType.compareTo("Stream") == 0)
                keyValue = preferences.getByteArray(key, new byte[]{});

            parsedKeys.put(keyName, keyValue);

        }
        return parsedKeys;
    }

    public byte[] getBytesPreferences() {
        return preferences.getByteArray("Bytes", new byte[]{});
    }

    public void setBytesPreferences(byte[] bytesPreferences) {
        preferences.putByteArray("Bytes", bytesPreferences);
    }
}

