package pl.edu.agh.pp.settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.util.HashMap;
import java.util.Random;
import java.util.prefs.BackingStoreException;

import static org.junit.Assert.*;

/**
 * Created by Maciej on 17.10.2016.
 * @author Maciej Makówka
 * 10:55
 * Project: server.
 */

public class OptionsTest {

    private Options options;

    @Before
    public void setUp() throws Exception {
        options = Options.getInstance();
        options.resetPreferences();
    }

    @Test
    public void testGetPreference() throws BackingStoreException, IllegalPreferenceObjectExpected {

        String key = "testGetPreference";
        Random random = new Random();
        int value = random.nextInt();

        HashMap<String, Object> currentPreference;
        currentPreference = options.getPreferences();

        currentPreference.put(key, value);
        options.setPreferences(currentPreference);

        Object getValue = options.getPreference(key, Integer.class);
        assertTrue("Currently preferences should contain key: testGetPreference", currentPreference.containsKey(key));
        assertNotNull("Check whether getPreference return something", getValue);
        assertEquals("Get value should be equal to set.", value, getValue);
    }

    @Test
    public void testGetPreferences() throws BackingStoreException, IllegalPreferenceObjectExpected {

        HashMap<String, Object> newPreferences = new HashMap<>();
        String[] keys = {"Key1", "Key2", "Key3", "Key4"};

        // Preference 1
        newPreferences.put(keys[0], true);
        // Preference 2
        newPreferences.put(keys[1], "Value2");
        // Preference 3
        newPreferences.put(keys[2], 69);
        // Preference 4
        byte[] preference4Value = {2, 3, 5};
        newPreferences.put(keys[3], preference4Value);

        options.setPreferences(newPreferences);

        HashMap<String, Object> preferences = options.getPreferences();
        assertNotNull("Preferences should have been loaded", preferences);

        String cntMsg = "Key should have been in Preferences after getting them";
        assertTrue(cntMsg, preferences.containsKey(keys[0]));
        assertTrue(cntMsg, preferences.containsKey(keys[1]));
        assertTrue(cntMsg, preferences.containsKey(keys[2]));
        assertTrue(cntMsg, preferences.containsKey(keys[3]));

        String sameMsg = "Key value should be equal after getting it from Preferences to this assigned";
        assertEquals(sameMsg, true, preferences.get(keys[0]));
        assertEquals(sameMsg, "Value2", preferences.get(keys[1]));
        assertEquals(sameMsg, 69, preferences.get(keys[2]));
        assertArrayEquals(sameMsg, preference4Value, (byte[]) preferences.get(keys[3]));

        String rmvMsg = "Removing preferences should be successfully";
        assertTrue(rmvMsg, preferences.remove(keys[0], true));
        assertTrue(rmvMsg, preferences.remove(keys[1], "Value2"));
        assertTrue(rmvMsg, preferences.remove(keys[2], 69));
        assertNotNull(rmvMsg, preferences.remove(keys[3]));
    }

    @Test
    public void testSetPreferences() throws BackingStoreException, IllegalPreferenceObjectExpected {

        HashMap<String, Object> beforePreferences = options.getPreferences();
        assertNotNull("Preferences should be loaded", beforePreferences);

        HashMap<String, Object> preferences = beforePreferences;

        for (String key : beforePreferences.keySet()) {
            Object beforeValue = beforePreferences.get(key);
            if (beforeValue instanceof Boolean)
                preferences.replace(key, beforeValue, true);
            if (beforeValue instanceof String)
                preferences.replace(key, beforeValue, "NewValue");
            if (beforeValue instanceof Integer)
                preferences.replace(key, beforeValue, 1);
            if (beforeValue instanceof byte[]) {
                byte[] newValue = {1, 2, 3};
                preferences.replace(key, beforeValue, newValue);
            }
        }


        boolean result = options.setPreferences(preferences);
        assertTrue("Should set preferences successfully", result);

        HashMap<String, Object> afterPreferences = options.getPreferences();
        assertNotNull("Preferences should be loaded", afterPreferences);
        for (String key : afterPreferences.keySet()) {
            Object afterValue = afterPreferences.get(key);
            if (afterValue instanceof Boolean)
                assertTrue("All Boolean preferences should be set on true value", (boolean) preferences.get(key));
            if (afterValue instanceof String)
                assertEquals("All String preferences should be set on 'NewValue' text",
                        "NewValue",
                        String.valueOf(preferences.get(key))
                );
            if (afterValue instanceof Integer)
                assertEquals("All Integer preferences should be set on '1'",
                        1,
                        (int) preferences.get(key)
                );
            if (afterValue instanceof byte[])
                assertArrayEquals("The byte stream should be equal to {1, 2, 3} stream",
                        new byte[]{1, 2, 3},
                        (byte[]) preferences.get(key)
                );
        }
    }

    @After
    public void tearDown() throws Exception {
        options.resetPreferences();
    }
}