package pl.edu.agh.pp.serializers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 19:08
 * server
 */
public class FileSerializer implements ISerializer
{
    private final Logger logger = LoggerFactory.getLogger(FileSerializer.class);

    private final String BASELINE_SERIALIZE_PATH = "baseline/";
    private final String ANOMALY_SERIALIZE_PATH = "anomalies/";

    private FileSerializer()
    {
        new File(BASELINE_SERIALIZE_PATH).mkdir();
        new File(ANOMALY_SERIALIZE_PATH).mkdir();
    }

    public static FileSerializer getInstance()
    {
        return Holder.INSTANCE;
    }

    @Override
    public String serializeBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline)
    {
        String timestamp = DateTime.now().toString("yyyy-MM-dd_HH-mm-ss");
        String filename = BASELINE_SERIALIZE_PATH + timestamp + ".ser";
        String infoFilename = BASELINE_SERIALIZE_PATH + timestamp + ".info";
        try (FileOutputStream fileOut = new FileOutputStream(filename);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                FileWriter fw = new FileWriter(new File(infoFilename));
                BufferedWriter bw = new BufferedWriter(fw))
        {
            out.writeObject(baseline);
            writeInfoContent(baseline, bw);
            return filename;
        }
        catch (Exception e)
        {
            logger.error("Error occurred while serializing baseline", e);
        }
        return null;
    }

    private void writeInfoContent(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, BufferedWriter bw) throws IOException
    {
        for (DayOfWeek day : baseline.keySet())
        {
            bw.write(day.toString());
            for (int routeId : baseline.get(day).keySet())
            {
                bw.write(" " + routeId);
            }
            bw.newLine();
        }
    }

    @Override
    public Map<DayOfWeek, Map<Integer, PolynomialFunction>> deserializeBaseline(String timestamp)
    {
        String filename = BASELINE_SERIALIZE_PATH + timestamp + ".ser";
        try (FileInputStream fileIn = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(fileIn))
        {
            return (Map<DayOfWeek, Map<Integer, PolynomialFunction>>) in.readObject();
        }
        catch (Exception e)
        {
            logger.error("Error occurred while deserializing baseline", e);
        }
        return new HashMap<>();
    }

    @Override
    public boolean doesBaselineFitConditions(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id)
    {
        return baseline.containsKey(dayOfWeek)
                && baseline.get(dayOfWeek).containsKey(id);
    }

    @Override
    public void serializeAnomalyTime(Map<Integer, DateTime> anomalyTime)
    {
        String filename = ANOMALY_SERIALIZE_PATH + "anomalyTime.ser";
        try (FileOutputStream fileOut = new FileOutputStream(filename);
                ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            out.writeObject(anomalyTime);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while serializing anomalyTime", e);
        }
    }

    @Override
    public void serializeAnomalyId(Map<Integer, String> anomalyId)
    {
        String filename = ANOMALY_SERIALIZE_PATH + "anomalyId.ser";
        try (FileOutputStream fileOut = new FileOutputStream(filename);
                ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            out.writeObject(anomalyId);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while serializing anomalyId", e);
        }
    }

    @Override
    public ConcurrentHashMap<Integer, DateTime> deserializeAnomalyTime()
    {
        String filename = ANOMALY_SERIALIZE_PATH + "anomalyTime.ser";
        try (FileInputStream fileIn = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(fileIn))
        {
            return (ConcurrentHashMap<Integer, DateTime>) in.readObject();
        }
        catch (Exception e)
        {
            logger.error("Error occurred while reading anomalyTime from file. Returning empty map.");
        }
        return new ConcurrentHashMap<>();
    }

    @Override
    public ConcurrentHashMap<Integer, String> deserializeAnomalyId()
    {
        String filename = ANOMALY_SERIALIZE_PATH + "anomalyId.ser";
        try (FileInputStream fileIn = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(fileIn))
        {
            return (ConcurrentHashMap<Integer, String>) in.readObject();
        }
        catch (Exception e)
        {
            logger.error("Error occurred while reading anomalyId from file. Returning empty map.");
        }
        return new ConcurrentHashMap<>();
    }

    private static class Holder
    {
        static final FileSerializer INSTANCE = new FileSerializer();
    }

}
