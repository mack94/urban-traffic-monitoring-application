package pl.edu.agh.pp.serializers;

import java.io.*;
import java.util.Map;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.joda.time.DateTime;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 19:08
 * server
 */
public class FileBaselineSerializer implements IBaselineSerializer
{
    private final String SERIALIZE_PATH = "baseline/";

    private FileBaselineSerializer()
    {
        new File(SERIALIZE_PATH).mkdir();
    }

    public static FileBaselineSerializer getInstance()
    {
        return Holder.INSTANCE;
    }

    @Override
    public String serialize(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline)
    {
        String timestamp = DateTime.now().toString("yyyy-MM-dd_HH-mm-ss");
        String filename = SERIALIZE_PATH + timestamp + ".ser";
        String infoFilename = SERIALIZE_PATH + timestamp + ".info";
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
            e.printStackTrace();
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
    public Map<DayOfWeek, Map<Integer, PolynomialFunction>> deserialize(String timestamp)
    {
        String filename = SERIALIZE_PATH + timestamp + ".ser";
        try (FileInputStream fileIn = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(fileIn))
        {
            return (Map<DayOfWeek, Map<Integer, PolynomialFunction>>) in.readObject();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean doesBaselineFitConditions(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id)
    {
        return baseline.containsKey(dayOfWeek)
                && baseline.get(dayOfWeek).containsKey(id);
    }

    private static class Holder
    {
        static final FileBaselineSerializer INSTANCE = new FileBaselineSerializer();
    }

}
