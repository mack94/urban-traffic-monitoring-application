package pl.edu.agh.pp.utils;

import com.google.maps.GeoApiContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-12.
 */
public class ContextLoaderTest {
    private ContextLoader contextLoader;
    private Field fileNameField;
    @Before
    public void setUp() throws Exception {
        contextLoader = new ContextLoader();
        fileNameField = contextLoader.getClass().getDeclaredField("propertiesFileName");
        fileNameField.setAccessible(true);
        fileNameField.set(contextLoader, "/test/test_config.properties");

    }

    @After
    public void tearDown() throws Exception {
        contextLoader = null;
    }

    @Test
    public void geoApiContextLoader() throws Exception {
        GeoApiContext geoApiContext = contextLoader.geoApiContextLoader();
        assertNotNull(geoApiContext);
    }

}