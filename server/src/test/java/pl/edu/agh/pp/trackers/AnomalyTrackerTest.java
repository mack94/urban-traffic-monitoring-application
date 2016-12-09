package pl.edu.agh.pp.trackers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.pp.utils.JodaTimeHelper;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by Krzysztof on 2016-11-05.
 */
public class AnomalyTrackerTest {

    private IOptions options;

    private AnomalyTracker anomalyTracker;
    @Before
    public void setUp() throws Exception {
        options = Options.getInstance();
        options.resetPreferences();
        anomalyTracker = AnomalyTracker.getInstance();
        //TODO use reflection to reset anomalyTracker fields
        resetAnomalyTracker();
    }

    @After
    public void tearDown() throws Exception {
        anomalyTracker = null;
        options.resetPreferences();
    }

    private void resetAnomalyTracker() throws NoSuchFieldException, IllegalAccessException {
        Field anomalyTimeField = anomalyTracker.getClass().getDeclaredField("anomalyTime");
        anomalyTimeField.setAccessible(true);
        anomalyTimeField.set(anomalyTracker, new ConcurrentHashMap<Integer, DateTime>() );

        Field anomalyID = anomalyTracker.getClass().getDeclaredField("anomalyID");
        anomalyID.setAccessible(true);
        anomalyID.set(anomalyTracker, new ConcurrentHashMap<Integer, Long>() );
    }

    @Test
    public void putAndGet() throws IllegalPreferenceObjectExpected {
        int routeID1 = 1;
        int routeID2 = 2;
        int anomalyLifeTime = (Integer) options.getPreference("AnomalyLifeTime", Integer.class);
        Random random = new Random();
        DateTime dateTime1 = new DateTime(2016, 1, 2, 0, 0, 0, DateTimeZone.UTC);
        DateTime dateTime2 = dateTime1.plusSeconds(random.nextInt(anomalyLifeTime));
        DateTime dateTime3 = new DateTime(2016, 1, 3, 0, 0, 0, DateTimeZone.UTC);
        DateTime dateTime4 = dateTime1.plusSeconds((Integer) options.getPreference("AnomalyLifeTime", Integer.class));
        DateTime dateTime5 = dateTime4.plusSeconds(anomalyLifeTime+1);

        assertEquals(anomalyTracker.put(routeID1, dateTime1), anomalyTracker.put(routeID1, dateTime2));
        String anomalyID1 = anomalyTracker.put(routeID1, dateTime3);
        assertEquals(anomalyID1, anomalyTracker.get(routeID1));
        assertEquals(-1, anomalyTracker.get(routeID2));

        String anomalyID2 = anomalyTracker.put(routeID2, dateTime1);
        assertEquals(anomalyID2, anomalyTracker.put(routeID2, dateTime4));
        assertNotEquals(anomalyID2, anomalyTracker.put(routeID2, dateTime5));
    }

    @Test
    public void getCurrentAnomaliesRoutesIds() {
        int routeID1 = 1;
        int routeID2 = 2;
        DateTime dateTime1 = new DateTime(2016, 1, 2, 0, 0, 0, DateTimeZone.UTC);
        assertTrue(anomalyTracker.getCurrentAnomaliesRoutesIds().isEmpty());
        anomalyTracker.put(routeID1, dateTime1);
        anomalyTracker.put(routeID2, dateTime1);
        List<Integer> routesList = anomalyTracker.getCurrentAnomaliesRoutesIds();
        assertEquals(routeID1, routesList.get(0).intValue());
        assertEquals(routeID2, routesList.get(1).intValue());
    }

    @Test
    public void has() {
        int routeID1 = 1;
        int routeID2 = 2;
        int routeID3 = 3;
        int routeID4 = 4;
        DateTime dateTime1 = new DateTime(2016, 1, 2, 0, 0, 0, DateTimeZone.UTC);
        anomalyTracker.put(routeID1, dateTime1);
        anomalyTracker.put(routeID2, dateTime1);
        anomalyTracker.put(routeID3, dateTime1);

        assertTrue(anomalyTracker.has(routeID1));
        assertTrue(anomalyTracker.has(routeID2));
        assertTrue(anomalyTracker.has(routeID3));
        assertFalse(anomalyTracker.has(routeID4));
    }

    @Test
    public void remove() throws IllegalPreferenceObjectExpected {
        Random random = new Random();
        int routeID1 = 1;
        DateTime dateTime1 = new DateTime(2016, 1, 2, 0, 0, 0, DateTimeZone.UTC);
        DateTime dateTime2 = dateTime1.plusSeconds(
                random.nextInt((Integer) options.getPreference("AnomalyLifeTime", Integer.class))/2);
        DateTime dateTime3 = dateTime2.plusSeconds(
                random.nextInt((Integer) options.getPreference("AnomalyLifeTime", Integer.class))/2);

        String anomalyID1 = anomalyTracker.put(routeID1, dateTime1);
        String anomalyID2 = anomalyTracker.put(routeID1, dateTime2);
        anomalyTracker.remove(routeID1);
        String anomalyID3 = anomalyTracker.put(routeID1, dateTime3);
        assertEquals(anomalyID1, anomalyID2);
        assertNotEquals(anomalyID1, anomalyID3);
    }

    @Test
    public void getLastUpdate() {
        int routeID1 = 1;
        int routeID2 = 2;
        DateTime dateTime1 = new DateTime(2016, 1, 2, 0, 0, 0, DateTimeZone.UTC);
        DateTime dateTime2 = new DateTime(2016, 1, 3, 0, 0, 0, DateTimeZone.UTC);
        DateTime dateTime3 = new DateTime(2016, 1, 4, 0, 0, 0, DateTimeZone.UTC);
        DateTime defualtDateTime = JodaTimeHelper.MINIMUM_ANOMALY_DATE;
        anomalyTracker.put(routeID1, dateTime1);
        assertEquals(dateTime1, anomalyTracker.getLastUpdate(routeID1));

        anomalyTracker.put(routeID1, dateTime2);
        anomalyTracker.put(routeID1, dateTime3);
        assertEquals(dateTime3, anomalyTracker.getLastUpdate(routeID1));

        assertEquals(defualtDateTime, anomalyTracker.getLastUpdate(routeID2));


    }

}