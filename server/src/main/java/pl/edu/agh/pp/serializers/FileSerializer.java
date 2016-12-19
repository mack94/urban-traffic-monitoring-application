package pl.edu.agh.pp.serializers;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
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
            logger.error("Cannot find file containing anomalyTime. Returning empty map.");
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
            logger.error("Cannot find file containing anomalyId. Returning empty map.");
        }
        return new ConcurrentHashMap<>();
    }


    public Map<DayOfWeek, Map<Integer, PolynomialFunction>> searchAndDeserializeBaseline(String timestamp, int routeID, AnomalyOperationProtos.DemandBaselineMessage.Day day)
    {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try{
            File baselineFolder = new File(BASELINE_SERIALIZE_PATH);
            if(!baselineFolder.isDirectory() || baselineFolder.listFiles().length==0){
                throw new FileNotFoundException("Baseline serialization folder does not exist or is empty");
            }
            File[] files = baselineFolder.listFiles();
            HashMap<Date, File> serializedBaselinesFiles = new HashMap<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            DateFormat timeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String filename;
            for (File file : files) {
                filename = file.getName();
                if (filename.endsWith(".ser")) {
                    serializedBaselinesFiles.put(dateFormat.parse(filename.split("\\.")[0]), file);
                }
            }
            Date timeStampDate = timeStampDateFormat.parse(timestamp);
            Collection<Date> serializedBaselineDates = serializedBaselinesFiles.keySet();
            List<Date> sortedByDiffDates = asSortedList(serializedBaselineDates, createComparator(timeStampDate));
            DayOfWeek weekDay = DayOfWeek.fromValue(day.getNumber());
            int index = 0;
            Date nearestDate = sortedByDiffDates.get(index);
            File serializedBaselines = serializedBaselinesFiles.get(nearestDate);
            fileIn = new FileInputStream(serializedBaselines);
            in = new ObjectInputStream(fileIn);
            Map<DayOfWeek, Map<Integer, PolynomialFunction>> baselines;
            baselines = (Map<DayOfWeek, Map<Integer, PolynomialFunction>>) in.readObject();


            while(!doesBaselineFitConditions(baselines, weekDay, routeID)){
                index++;
                if(index > sortedByDiffDates.size()-1) break;
                nearestDate = sortedByDiffDates.get(index);
                serializedBaselines = serializedBaselinesFiles.get(nearestDate);
                fileIn = new FileInputStream(serializedBaselines);
                in = new ObjectInputStream(fileIn);
                baselines = (Map<DayOfWeek, Map<Integer, PolynomialFunction>>) in.readObject();
            }

            return baselines;
        }
        catch (Exception e)
        {
            logger.error("Error occurred while searching for and deserializing baseline", e);
        }
        finally {
            try {
                fileIn.close();
                in.close();
            } catch (Exception e) {
                logger.error("Error occurred while closing input streams after serialization/deserialization", e);
            }
        }
        return new HashMap<>();
    }

    private static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c, Comparator<T> comp) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list, comp);
        return list;
    }

    private static Comparator<Date> createComparator(Date targetBaselineDate)
    {
        final Date finalDate = targetBaselineDate;
        return (d0, d1) -> {
            long finalTime = finalDate.getTime();
            long dateDiff0 = Math.abs(finalTime - d0.getTime());
            long dateDiff1 = Math.abs(finalTime - d1.getTime());
            return Long.compare(dateDiff0, dateDiff1);
        };
    }


    private static class Holder
    {
        static final FileSerializer INSTANCE = new FileSerializer();
    }

}
