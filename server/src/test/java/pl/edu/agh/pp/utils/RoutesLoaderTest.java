package pl.edu.agh.pp.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
/**
 * Created by Krzysztof Węgrzyński on 2016-11-12.
 */
public class RoutesLoaderTest {
    private String defualtFileName;
    private Field fileNameField;
    private RoutesLoader instance;
    @Before
    public void setUp() throws Exception {
        instance = RoutesLoader.getInstance();
        fileNameField = RoutesLoader.class.getDeclaredField("fileName");
        fileNameField.setAccessible(true);
        defualtFileName = instance.getDefaultJSONFileName();
        fileNameField.set(RoutesLoader.getInstance(), "test_routes.json");
    }

    @After
    public void tearDown() throws Exception {
        fileNameField.set(RoutesLoader.getInstance(), defualtFileName);
    }

    @Test
    public void loadJSON() throws IOException {
        JSONArray loadedRoutes = instance.loadJSON();
        assertNotNull(loadedRoutes);

        JSONObject route = loadedRoutes.getJSONObject(0);

        assertEquals("1", route.get("id").toString());

        assertEquals("50.064690, 19.923898", route.get("destination").toString());

        assertEquals("Slowackiego 66, Krakow", route.get("origin").toString());

        assertEquals("50.073790,19.935490; 50.073650,19.934180; 50.073530,19.933700; 50.073310,19.932830;" +
                " 50.073180,19.932450; 50.072970,19.931960; 50.072720,19.931430; 50.072570,19.931140;" +
                " 50.071850,19.929740; 50.071560,19.929070; 50.071430,19.928850; 50.071270,19.928550;" +
                " 50.071160,19.928370; 50.070700,19.927590; 50.070460,19.927200; 50.070340,19.927070;" +
                " 50.070090,19.926850; 50.069430,19.926460; 50.069090,19.926210; 50.068610,19.925850;" +
                " 50.067530,19.925130; 50.067010,19.924820; 50.066040,19.924380; 50.065590,19.924230;" +
                " 50.064690,19.923930;", route.getString("coords"));
    }

}