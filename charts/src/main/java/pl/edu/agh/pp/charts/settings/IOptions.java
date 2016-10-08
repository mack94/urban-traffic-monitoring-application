package pl.edu.agh.pp.charts.settings;

import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.IOException;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Maciej on 08.10.2016.
 * 16:26
 * Project: charts.
 */

public interface IOptions {

    /**
     * Method that initialize the environment. It load default preferences.
     *
     * @throws BackingStoreException
     * @throws IOException
     * @see Options
     * @see Configuration
     * @see Preferences
     */
    void initialize() throws BackingStoreException, IOException;

    /**
     * Method that returns value (as an Object: String, boolean, int) of the property which key is equal to given.
     *
     * @param key                 Put the String key which represents desired preference (with key name).
     * @param expectedObjectClass Put the object's class which specify what type of response do you need. For instance
     *                            you may need, from registry, the preference with name: key returned value: boolean
     *                            Then you call the method like example: .getPreference("Key", Boolean.class);
     *                            (One key preference may assign to many preferences in different type)
     * @return property value
     * @see java.util.prefs.Preferences
     * @see Configuration
     * @see Options
     */
    Object getPreference(String key, Class expectedObjectClass) throws IllegalPreferenceObjectExpected;

    /**
     * Method which task is returning all user preferences in smart form.
     *
     * @return HashMap with properties <Key - Value>
     * @throws BackingStoreException
     * @see BackingStoreException
     * @see java.util.prefs.Preferences
     * @see Configuration
     * @see Options
     */
    HashMap<String, Object> getPreferences() throws BackingStoreException;

    /**
     * Method that allow us to set selected properties.
     *
     * @param preferences HashMap with preferences. Preferences put into using pattern: HashMap<Key, Value>
     * @return The result of setting preferences (Success(=True) or an error(=False))
     * @see java.util.prefs.Preferences
     * @see Configuration
     * @see Options
     */
    boolean setPreferences(HashMap<String, Object> preferences);

    /**
     * Method that allow to reset all user preferences - restore the default state of all settings.
     *
     * @return The result of resetting preferences (Success(=True) or an error(=False))
     * @see java.util.prefs.Preferences
     * @see Configuration
     * @see Options
     */
    boolean resetPreferences() throws BackingStoreException, IOException;

    /**
     * Method that allows to remove user preference.
     *
     * @param key Put here the Preference key to be removed.
     * @return The result of removing preference (Success(=True) or an error(=False))
     * @see java.util.prefs.Preferences
     * @see Configuration
     * @see Options
     */
    boolean removePreferences(String key) throws IllegalPreferenceObjectExpected;

    /**
     * Method that returns bytes array with all the preferences stored as a bytes array.
     * <br>
     * **HowTo**: Short tutorial how to read the bytes array from preferences
     * <pre>
     * {@code
     *   byte[] bytesPreferences = Options.getInstance().getBytesPreferences();
     *   ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesPreferences);
     *   ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
     *   Map<String, Map<String, Boolean>> readHash;
     *   readHash = (Map<String, Map<String, Boolean>>) objectInputStream.readObject();
     *   }
     * </pre>
     *
     * @return bytes array with all bytes array style Preferences
     * @see Preferences
     * @see Options
     * @see Configuration
     */
    byte[] getBytesPreferences();

    /**
     * Method that allows to set preferences which are stored as a bytes array.
     *
     * @param bytesPreferences The set of preferences that are serializable and saved as bytes array. It it
     *                         useful for nested preferences, which are not only one dimensional.
     * @see Preferences
     * @see Options
     * @see Configuration
     */
    void setBytesPreferences(byte[] bytesPreferences);

    /**
     * Method that returns the name of the key in a group of bytes options which has true value.
     *
     * @param key Is the key to the group of options in preferences which true value key need to be returned.
     * @return String with the Key of operation i byte array which has (in group) first true value. If no one value in group
     * is true, then null is returned.
     * @see Preferences
     * @see Options
     * @see Configuration
     */
    String getFirstTruePreferenceFromPreferencesGroup(String key);

    /**
     * Method that set, in group of options in one group of preferences (saved as byte[]), one option on true.
     * Other options in group are false
     *
     * @param group Group of preferences in which the method will look for the key.
     * @param key   Key of which value in Map should be set as true.
     * @return true if the operation was a success.
     * @see Preferences
     * @see Options
     * @see Configuration
     */
    boolean setOnlyTruePreferenceInPreferencesGroup(String group, String key);
}

