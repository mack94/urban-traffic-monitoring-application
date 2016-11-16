package pl.edu.agh.pp.charts.persistence;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.pp.charts.data.local.Record;
import pl.edu.agh.pp.charts.data.local.ResourcesHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-15.
 */
public class PersistenceManagerTest {
    private ResourcesHolder resourcesHolder;
    private String defaultPath;
    private Field pathField;

    @Before
    public void setUp() throws Exception {
        resourcesHolder = ResourcesHolder.getInstance();
        pathField = resourcesHolder.getClass().getDeclaredField("path");
        pathField.setAccessible(true);
        defaultPath = resourcesHolder.getPath();
        pathField.set(resourcesHolder, "test_"+defaultPath);
    }

    @After
    public void tearDown() throws Exception {
        File testDir = new File(resourcesHolder.getPath().split("/")[0]);
        FileUtils.deleteDirectory(testDir);
        pathField.set(resourcesHolder, defaultPath);
    }

    private void setRecordUp(Record record, JSONObject json) {
        record.setDate(json.getString("timeStamp"));
        record.setId(json.getString("id"));
        record.setDistance(json.getString("distance"));
        record.setDuration(json.getString("duration"));
        record.setDurationInTraffic(json.getString("durationInTraffic"));
        record.setTime();
    }

    private List<Record> setRecordsUp() {
        JSONObject json1 = new JSONObject("{\"timeStamp\":\"2016-11-14 00:04:03,798\",\"duration\":\"230\",\"durationInTraffic\":\"185\",\"distance\":\"1,4 km\",\"id\":\"1\",\"isAnomaly\":false,\"waypoints\":\"default\"}");
        JSONObject json2 = new JSONObject("{\"timeStamp\":\"2016-11-14 00:04:03,930\",\"duration\":\"176\",\"durationInTraffic\":\"147\",\"distance\":\"1,3 km\",\"id\":\"2\",\"isAnomaly\":false,\"waypoints\":\"default\"}");
        JSONObject json3 = new JSONObject("{\"timeStamp\":\"2016-11-14 00:04:04,056\",\"duration\":\"168\",\"durationInTraffic\":\"165\",\"distance\":\"2,2 km\",\"id\":\"3\",\"isAnomaly\":false,\"waypoints\":\"default\"}");
        JSONObject json4 = new JSONObject("{\"timeStamp\":\"2016-11-13 00:04:04,599\",\"duration\":\"230\",\"durationInTraffic\":\"192\",\"distance\":\"1,4 km\",\"id\":\"1\",\"isAnomaly\":false,\"waypoints\":\"default\"}");
        JSONObject json5 = new JSONObject("{\"timeStamp\":\"2016-10-31 00:04:03,806\",\"duration\":\"227\",\"durationInTraffic\":\"183\",\"distance\":\"1,4 km\",\"id\":\"1\",\"waypoints\":\"[aleja Adama Mickiewicza i aleja Juliusza Słowackiego/Aleje Trzech Wieszczów/II obwodnica],  [50.064690,19.923930; 50.063810,19.923610; 50.063760,19.924040; 50.063850,19.924070; 50.064770,19.924370; 50.065090,19.924500; 50.065950,19.924870; 50.067110,19.925370; 50.067390,19.925530; 50.067970,19.925920; 50.068850,19.926500; 50.069130,19.926720; 50.069640,19.927040; 50.070070,19.927350; 50.070290,19.927550; 50.070390,19.927670; 50.070710,19.928260; 50.070960,19.928730; 50.071360,19.929470; 50.071640,19.930000; 50.072480,19.931580; 50.072690,19.932050; 50.072940,19.932650; 50.073040,19.932950; 50.073300,19.933870; 50.073390,19.934270; 50.073470,19.935030; 50.073530,19.935800; 50.073700,19.935770; 50.073820,19.935760; 50.073790,19.935500; 50.073790,19.935490; ]\"}");
        JSONObject json6 = new JSONObject("{\"timeStamp\":\"2016-11-14 00:09:03,757\",\"duration\":\"230\",\"durationInTraffic\":\"188\",\"distance\":\"1,4 km\",\"id\":\"1\",\"isAnomaly\":false,\"waypoints\":\"default\"}");
        Record record1 = new Record();
        Record record2 = new Record();
        Record record3 = new Record();
        Record record4 = new Record();
        Record record5 = new Record();
        Record record6 = new Record();

        setRecordUp(record1, json1);
        setRecordUp(record2, json2);
        setRecordUp(record3, json3);
        setRecordUp(record4, json4);
        setRecordUp(record5, json5);
        setRecordUp(record6, json6);

        List<Record> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);
        records.add(record3);
        records.add(record4);
        records.add(record5);
        records.add(record6);

        return records;
    }

    @Test
    public void saveToFiles() throws Exception {
        PersistenceManager persistenceManager = new PersistenceManager();

        persistenceManager.saveToFiles(setRecordsUp());
        File testDir = new File("test_"+defaultPath);
        assertTrue(testDir.isDirectory());
        assertEquals(5, testDir.listFiles().length);


    }

    @Test
    public void readFromFile() throws Exception {
        PersistenceManager persistenceManager = new PersistenceManager();
        persistenceManager.saveToFiles(setRecordsUp());
        Map<Double, Double> resultMap = persistenceManager.readFromFile("MON", "1", false);
        assertTrue(resultMap.values().contains(230.0));
        assertFalse(resultMap.values().contains(227.0));
        resultMap = persistenceManager.readFromFile("MON", "1", true);
        assertTrue(resultMap.values().contains(185.0));

    }

    @Test
    public void readFromFiles() throws Exception {
        PersistenceManager persistenceManager = new PersistenceManager();
        persistenceManager.saveToFiles(setRecordsUp());
        Map<Double, Double> resultMap = persistenceManager.readFromFiles("MON", "1", false);
        assertTrue(resultMap.values().contains(228.5));
        resultMap = persistenceManager.readFromFile("MON", "1", true);
        //System.out.println(Arrays.toString(resultMap.values().toArray()));
        assertTrue(resultMap.values().contains(184.0));
        assertTrue(resultMap.values().contains(188.0));
    }

    @Test
    public void removeFiles() throws Exception {
        PersistenceManager persistenceManager = new PersistenceManager();
        persistenceManager.saveToFiles(setRecordsUp());
        File testDir = new File(resourcesHolder.getPath().split("/")[0]);
        assertTrue(testDir.isDirectory());
        persistenceManager.removeFiles();
        assertFalse(testDir.isDirectory());
    }

}