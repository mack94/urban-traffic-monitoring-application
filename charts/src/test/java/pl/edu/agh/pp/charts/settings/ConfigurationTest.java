package pl.edu.agh.pp.charts.settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.lang.reflect.Field;
import java.util.prefs.Preferences;

import static org.junit.Assert.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-01.
 */
public class ConfigurationTest {
    private Configuration configuration;


    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
    }

    @After
    public void tearDown() throws Exception {
        configuration = null;
    }

    @Ignore ("printing to std out through logger, nothing to test")
    @Test
    public void printPreferences() throws Exception {

    }


    @Test
    public void resetPreferences() throws Exception {

    }

    @Test
    public void getPreferences() throws Exception {

    }

    @Test
    public void setPreferences() throws Exception {

    }

    @Test
    public void getPreference() throws Exception {

    }

    @Test
    public void removePreference() throws Exception {

    }

    @Test
    public void getBytesPreferences() throws Exception {

    }

    @Test
    public void setBytesPreferences() throws Exception {

    }

}
