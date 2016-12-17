package pl.edu.agh.pp.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.builders.IPatternBuilder;
import pl.edu.agh.pp.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.loaders.FilesLoader;
import pl.edu.agh.pp.serializers.FileSerializer;
import pl.edu.agh.pp.serializers.ISerializer;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.utils.AnomalyLifeTimeInfoHelper;
import pl.edu.agh.pp.utils.ApisHelper;
import pl.edu.agh.pp.utils.AvailableHistoricalInfoHelper;
import pl.edu.agh.pp.utils.BaselineWindowSizeInfoHelper;
import pl.edu.agh.pp.utils.ContextLoader;
import pl.edu.agh.pp.utils.DayRequestsFrequencyInfoHelper;
import pl.edu.agh.pp.utils.DayShiftStartInfoHelper;
import pl.edu.agh.pp.utils.ExpirationBroadcastInfoHelper;
import pl.edu.agh.pp.utils.ExpirationIntervalInfoHelper;
import pl.edu.agh.pp.utils.LeverInfoHelper;
import pl.edu.agh.pp.utils.NightRequestsFrequencyInfoHelper;
import pl.edu.agh.pp.utils.NightShiftStartInfoHelper;
import pl.edu.agh.pp.utils.RepeaterIntervalInfoHelper;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 20:35
 * server
 */
public class CommandLineManager extends Thread
{
    private static final IPatternBuilder patternBuilder = PolynomialPatternBuilder.getInstance();
    private static final ISerializer baselineSerializer = FileSerializer.getInstance();
    private static IOptions options = Options.getInstance();
    private final Logger logger = LoggerFactory.getLogger(CommandLineManager.class);
    private final Map<String, DayOfWeek> daysOfWeek = getDaysMap();

    @Override
    public void run()
    {
        String buffer;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            try
            {
                buffer = in.readLine();
            }
            catch (Exception e)
            {
                System.out.println("Error occurred while reading input: " + e);
                continue;
            }
            logger.info("Typed command: {}", buffer);
            // counts new baseline of files in logs/
            if (buffer.equals("COUNT_BASELINE"))
            {
                countBaseline();
            }
            // loads baseline from file only for given day and route
            else if (buffer.startsWith("LOAD_BASELINE_PARTIALLY"))
            {
                loadBaselinePartially(buffer);
            }
            // loads baseline from file and replaces in PatternBuilder
            else if (buffer.startsWith("LOAD_BASELINE"))
            {
                loadBaseline(buffer);
            }
            // updates current baseline with data contained in given file
            else if (buffer.startsWith("UPDATE_BASELINE"))
            {
                updateBaseline(buffer);
            }
            else if (buffer.startsWith("SET_LEVER"))
            {
                setLever(buffer);
            }
            else if (buffer.startsWith("SET_ANOMALY_LIFE_TIME"))
            {
                setAnomalyLifeTime(buffer);
            }
            else if (buffer.startsWith("SET_BASELINE_WINDOW_SIZE"))
            {
                setBaselineWindowSize(buffer);
            }
            else if (buffer.startsWith("RESET_PREFERENCES"))
            {
                resetPreferences();
            }
            else if (buffer.startsWith("CHANGE_API_KEY"))
            {
                changeDetectorApiKey(buffer);
            }
            else if (buffer.startsWith("CHANGE_MAPS_API_KEY"))
            {
                changeMapsApiKey(buffer);
            }
            else if (buffer.startsWith("SET_REPEATER_INTERVAL"))
            {
                setRepeaterInterval(buffer);
            }
            else if (buffer.startsWith("SET_EXPIRATION_INTERVAL"))
            {
                setExpirationInterval(buffer);
            }
            else if (buffer.startsWith("SET_EXPIRATION_BROADCAST"))
            {
                setExpirationBroadcast(buffer);
            }
            else if (buffer.startsWith("SET_DAY_REQUESTS_FREQ"))
            {
                setDayRequestsFrequency(buffer);
            }
            else if (buffer.startsWith("SET_NIGHT_REQUESTS_FREQ"))
            {
                setNightRequestsFrequency(buffer);
            }
            else if (buffer.startsWith("SET_DAY_SHIFT_START"))
            {
                setDayShiftStart(buffer);
            }
            else if (buffer.startsWith("SET_NIGHT_SHIFT_START"))
            {
                setNightShiftStart(buffer);
            }
            else if (buffer.startsWith("AV_H"))
            {
                System.out.println(AvailableHistoricalInfoHelper.getAvailableDateRoutes().keySet());
            }
            else if (buffer.equalsIgnoreCase("help") || buffer.equalsIgnoreCase("-help") ||
                    buffer.equalsIgnoreCase("-h") || buffer.equalsIgnoreCase("h"))
            {
                System.out.println("COUNT_BASELINE - counts new baseline of files in logs directory");
                System.out.println("LOAD_BASELINE_PARTIALLY - loads baseline from file only for given day and route");
                System.out.println("LOAD_BASELINE - loads baseline from file and replaces in PatternBuilder");
                System.out.println("UPDATE_BASELINE - updates current baseline with data contained in given file");
                System.out.println("SET_LEVER - changes the lever value");
                System.out.println("SET_ANOMALY_LIFE_TIME - changes time needed for anomaly to expire");
                System.out.println("SET_BASELINE_WINDOW_SIZE - changes baseline window size");
                System.out.println("RESET_PREFERENCES - resets current server preferences to default state");
                System.out.println("AV_H - lists available dates for historical data");
                System.out.println("CHANGE_API_KEY - changes the key used by Detector module for requests to google APIS");
                System.out.println("CHANGE_MAPS_API_KEY - changes the key used by Maps module for requests to google APIS");
                System.out.println("SET_DAY_SHIFT_START - changes the time when day shift starts");
                System.out.println("SET_NIGHT_SHIFT_START - changes the time when night shift starts");
                System.out.println("SET_DAY_REQUESTS_FREQ - changes range of time that is base for generating intervals between executing API requests during day shift");
                System.out.println("SET_NIGHT_REQUESTS_FREQ - changes range of time that is base for generating intervals between executin API requests during night shift");
                System.out.println("SET_REPEATER_INTERVAL - changes interval between execution time of anomaly repeater");
                System.out.println("SET_EXPIRATION_INTERVAL - changes interval between execution time of expiration listener");
                System.out.println("SET_EXPIRATION_BROADCAST_TIME - changes time of broadcasting information about anomaly expiration");
                System.out.println("help - displays commands list");
            }
            else
            {
                System.out.println("Command not found. type help to get the list of possible commands.");
            }
        }
    }

    private void countBaseline()
    {
        try
        {
            File baselineDir = new File("logs/");
            String[] filenames = Arrays.stream(baselineDir.listFiles())
                    .map(f -> "logs/" + f.getName())
                    .toArray(String[]::new);
            FilesLoader filesLoader = new FilesLoader(filenames);
            PolynomialPatternBuilder.computePolynomial(filesLoader.processLineByLine(), false);
        }
        catch (Exception e)
        {
            System.out.println("Error occurred while counting baseline: " + e);
        }
    }

    private void loadBaselinePartially(String buffer)
    {
        try
        {
            String[] params = StringUtils.removeStart(buffer, "LOAD_BASELINE_PARTIALLY ").split(" ");
            DayOfWeek dayOfWeek = getDayOfWeek(params[0]);
            int id = Integer.valueOf(params[1]);
            String timestamp = params[2];
            Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserializeBaseline(timestamp);
            if (baseline != null && baselineSerializer.doesBaselineFitConditions(baseline, dayOfWeek, id))
            {
                patternBuilder.setPartialBaseline(baseline, dayOfWeek, id, timestamp);
                logger.info("Baseline {} for route {} on {} has been set successfully", timestamp, id, dayOfWeek);
            }
            else
            {
                logger.warn("Command parameters are incorrect - cannot find baseline for given route on given day.\nCommand is being ignored.");
            }
        }
        catch (Exception e)
        {
            logger.error("Error occurred while loading baseline", e);
        }
    }

    private void loadBaseline(String buffer)
    {
        try
        {
            String timestamp = StringUtils.removeStart(buffer, "LOAD_BASELINE ");
            Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserializeBaseline(timestamp);
            if (baseline != null)
            {
                patternBuilder.setBaseline(baseline, timestamp);
                logger.info("Baseline {} has been set successfully", timestamp);
            }
            else
            {
                logger.warn("Command parameter is incorrect - cannot read baseline from given file\nCommand is being ignored.");
            }
        }
        catch (Exception e)
        {
            logger.error("Error occurred while loading baseline", e);
        }
    }

    private void updateBaseline(String buffer)
    {
        try
        {
            String timestamp = StringUtils.removeStart(buffer, "UPDATE_BASELINE ");
            Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserializeBaseline(timestamp);
            if (baseline != null)
            {
                patternBuilder.updateBaseline(baseline, timestamp);
                logger.info("Current baseline has been successfully updated by {}", timestamp);
            }
            else
            {
                logger.warn("Command parameter is incorrect - cannot read baseline from given file\nCommand is being ignored.");
            }
        }
        catch (Exception e)
        {
            logger.error("Error occurred while updating baseline", e);
        }
    }

    private void setLever(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_LEVER ");
            String[] args = buffer.split(" ");
            int percentLeverValue = Integer.parseInt(args[0]);
            if (percentLeverValue < -25 || percentLeverValue > 100)
            {
                throw new IllegalArgumentException("Wrong parameter");
            }
            LeverInfoHelper.getInstance().setLeverValue(percentLeverValue);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting lever", e);
        }
    }

    private void setAnomalyLifeTime(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_ANOMALY_LIFE_TIME ");
            String[] args = buffer.split(" ");
            int secondsAnomalyLifeTime = Integer.parseInt(args[0]);
            if (secondsAnomalyLifeTime < 0)
            {
                throw new IllegalArgumentException("Wrong parameter");
            }
            int expirationInterval = ExpirationIntervalInfoHelper.getInstance().getExpirationIntervalValue();
            if (secondsAnomalyLifeTime <= expirationInterval)
            {
                throw new IllegalArgumentException("Anomaly life time cannot be less than anomaly expiration interval");
            }
            AnomalyLifeTimeInfoHelper.getInstance().setAnomalyLifeTimeValue(secondsAnomalyLifeTime);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting anomaly life time", e);
        }
    }

    private void setBaselineWindowSize(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_BASELINE_WINDOW_SIZE ");
            String[] args = buffer.split(" ");
            int baselineWindowSize = Integer.parseInt(args[0]);
            if (baselineWindowSize < 0 || baselineWindowSize > 1440)
            {
                throw new IllegalArgumentException("Wrong parameter");
            }
            BaselineWindowSizeInfoHelper.getInstance().setBaselineWindowSizeValue(baselineWindowSize);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting baseline window size", e);
        }
    }

    private void resetPreferences()
    {
        try
        {
            boolean result = options.resetPreferences();
            logger.info("Preferences reset - ", result);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while resetting preferences", e);
        }
    }

    private void changeDetectorApiKey(String buffer)
    {
        try
        {
            String key = StringUtils.removeStart(buffer, "CHANGE_API_KEY ");
            ContextLoader.changeApiKey(key);
            ApisHelper.getInstance().setDetectorApiKey(key);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while changing API key", e);
        }
    }

    private void changeMapsApiKey(String buffer)
    {
        try
        {
            String key = StringUtils.removeStart(buffer, "CHANGE_MAPS_API_KEY ");
            ApisHelper.getInstance().setMapsApiKey(key);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while changing API key", e);
        }
    }

    private void setRepeaterInterval(String buffer)
    {
        try
        {
            String interval = StringUtils.removeStart(buffer, "SET_REPEATER_INTERVAL ");
            if (Integer.valueOf(interval) < 0)
            {
                throw new IllegalArgumentException("Interval must be positive");
            }
            RepeaterIntervalInfoHelper.getInstance().setRepeaterIntervalValue(Integer.valueOf(interval));
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting repeater interval", e);
        }
    }

    private void setExpirationInterval(String buffer)
    {
        try
        {
            String interval = StringUtils.removeStart(buffer, "SET_EXPIRATION_INTERVAL ");
            int expirationInterval = Integer.valueOf(interval);
            if (expirationInterval < 0)
            {
                throw new IllegalArgumentException("Interval must be positive");
            }
            int anomalyLifeTime = AnomalyLifeTimeInfoHelper.getInstance().getAnomalyLifeTimeValue();
            if (expirationInterval >= anomalyLifeTime)
            {
                throw new IllegalArgumentException("Anomaly expiration interval cannot be greater than anomaly life time");
            }
            ExpirationIntervalInfoHelper.getInstance().setExpirationIntervalValue(Integer.valueOf(interval));
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting expiration interval", e);
        }
    }

    private void setExpirationBroadcast(String buffer)
    {
        try
        {
            String value = StringUtils.removeStart(buffer, "SET_EXPIRATION_BROADCAST ");
            if (Integer.valueOf(value) < 0)
            {
                throw new IllegalArgumentException("Value must be positive");
            }
            ExpirationBroadcastInfoHelper.getInstance().setExpirationBroadcastValue(Integer.valueOf(value));
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting expiration broadcast", e);
        }
    }

    private void setDayRequestsFrequency(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_DAY_REQUESTS_FREQ ");
            String[] values = buffer.split(" ");
            if (values.length != 2)
            {
                throw new IllegalArgumentException("Expected 2 parameters, found " + values.length);
            }
            int from = Integer.valueOf(values[0]);
            int to = Integer.valueOf(values[1]);
            if (from < 0 || to < 0 || from > to)
            {
                throw new IllegalArgumentException("Wrong parameters");
            }
            DayRequestsFrequencyInfoHelper.getInstance().setFrequenciesBounds(from, to);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting requests frequency for day shift", e);
        }
    }

    private void setNightRequestsFrequency(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_NIGHT_REQUESTS_FREQ ");
            String[] values = buffer.split(" ");
            if (values.length != 2)
            {
                throw new IllegalArgumentException("Expected 2 parameters, found " + values.length);
            }
            int from = Integer.valueOf(values[0]);
            int to = Integer.valueOf(values[1]);
            if (from < 0 || to < 0 || from > to)
            {
                throw new IllegalArgumentException("Wrong parameters");
            }
            NightRequestsFrequencyInfoHelper.getInstance().setFrequenciesBounds(from, to);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting requests frequency for night shift", e);
        }
    }

    private void setDayShiftStart(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_DAY_SHIFT_START");
            String[] values = buffer.split(":");
            buffer = parseHour(values, buffer);
            DayShiftStartInfoHelper.getInstance().setDayShiftStart(buffer);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting day shift start", e);
        }
    }

    private void setNightShiftStart(String buffer)
    {
        try
        {
            buffer = StringUtils.removeStart(buffer, "SET_NIGHT_SHIFT_START");
            String[] values = buffer.split(":");
            buffer = parseHour(values, buffer);
            NightShiftStartInfoHelper.getInstance().setNightShiftStart(buffer);
        }
        catch (Exception e)
        {
            logger.error("Error occurred while setting night shift start", e);
        }
    }

    private String parseHour(String[] values, String buffer)
    {
        if (values.length < 2 && values.length > 3)
        {
            throw new IllegalArgumentException("Wrong time format. Expected HH:mm ot HH:mm:ss");
        }
        int hour = Integer.valueOf(values[0]);
        if (hour < 0 || hour > 23)
        {
            throw new IllegalArgumentException("Hour must be between <0; 23>");
        }
        int minute = Integer.valueOf(values[1]);
        if (minute < 0 || minute > 59)
        {
            throw new IllegalArgumentException("Minute must be between <0; 59>");
        }
        int second = 0;
        if (values.length == 3)
        {
            second = Integer.valueOf(values[2]);
        }
        if (second < 0 || second > 59)
        {
            throw new IllegalArgumentException("Second must be between <0; 59>");
        }
        if (values.length == 2)
        {
            buffer = new StringBuilder(values[0])
                    .append(":")
                    .append(values[1])
                    .append(":")
                    .append("00")
                    .toString();
        }
        return buffer;
    }

    private DayOfWeek getDayOfWeek(String day)
    {
        return daysOfWeek.get(day);
    }

    private Map<String, DayOfWeek> getDaysMap()
    {
        Map<String, DayOfWeek> map = new HashMap<>();
        map.put("Mon", DayOfWeek.MONDAY);
        map.put("Tue", DayOfWeek.TUESDAY);
        map.put("Wed", DayOfWeek.WEDNESDAY);
        map.put("Thu", DayOfWeek.THURSDAY);
        map.put("Fri", DayOfWeek.FRIDAY);
        map.put("Sat", DayOfWeek.SATURDAY);
        map.put("Sun", DayOfWeek.SUNDAY);
        return map;
    }

}
