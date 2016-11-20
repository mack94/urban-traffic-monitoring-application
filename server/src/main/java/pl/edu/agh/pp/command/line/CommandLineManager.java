package pl.edu.agh.pp.command.line;

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
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import pl.edu.agh.pp.utils.AnomalyLiveTimeInfoHelper;
import pl.edu.agh.pp.utils.AvailableHistoricalInfoHelper;
import pl.edu.agh.pp.utils.BaselineWindowSizeInfoHelper;
import pl.edu.agh.pp.utils.LeverInfoHelper;
import pl.edu.agh.pp.loaders.FilesLoader;
import pl.edu.agh.pp.serializers.FileBaselineSerializer;
import pl.edu.agh.pp.serializers.IBaselineSerializer;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 20:35
 * server
 */
public class CommandLineManager extends Thread
{
    private static final IPatternBuilder patternBuilder = PolynomialPatternBuilder.getInstance();
    private static final IBaselineSerializer baselineSerializer = FileBaselineSerializer.getInstance();
    private static final LeverInfoHelper leverInfoHelper = LeverInfoHelper.getInstance();
    private static final AnomalyLiveTimeInfoHelper anomalyLiveTimeInfoHelper = AnomalyLiveTimeInfoHelper.getInstance();
    private static final BaselineWindowSizeInfoHelper baselineWindowSizeInfoHelper = BaselineWindowSizeInfoHelper.getInstance();
    private static IOptions options = Options.getInstance();
    private final Logger logger = (Logger) LoggerFactory.getLogger(CommandLineManager.class);
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
                // counts new baseline of files in logs/
                if (buffer.equals("count_baseline"))
                {
                    File baselineDir = new File("logs/");
                    String[] filenames = Arrays.stream(baselineDir.listFiles())
                            .map(f -> "logs/" + f.getName())
                            .toArray(String[]::new);
                    FilesLoader filesLoader = new FilesLoader(filenames);
                    PolynomialPatternBuilder.computePolynomial(filesLoader.processLineByLine(), false);
                }
                // loads baseline from file only for given day and route
                else if (buffer.startsWith("load_partially "))
                {
                    String[] params = StringUtils.removeStart(buffer, "load_partially ").split(" ");
                    DayOfWeek dayOfWeek = getDayOfWeek(params[0]);
                    int id = Integer.valueOf(params[1]);
                    String timestamp = params[2];
                    Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserialize(timestamp);
                    if (baseline != null && baselineSerializer.doesBaselineFitConditions(baseline, dayOfWeek, id))
                    {
                        patternBuilder.setPartialBaseline(baseline, dayOfWeek, id);
                    }
                    else
                    {
                        logger.warn("Command parameters are incorrect - cannot find baseline for given route on given day\nCommand is being ignored.");
                    }
                }
                // loads baseline from file and replaces in PatternBuilder
                else if (buffer.startsWith("load "))
                {
                    String timestamp = StringUtils.removeStart(buffer, "load ");
                    Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserialize(timestamp);
                    if (baseline != null)
                    {
                        patternBuilder.setBaseline(baseline);
                    }
                    else
                    {
                        logger.warn("Command parameter is incorrect - cannot read baseline from given file\nCommand is being ignored.");
                    }
                }
                // updates current baseline with data contained in given file
                else if (buffer.startsWith("update_baseline "))
                {
                    String timestamp = StringUtils.removeStart(buffer, "update_baseline");
                    Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = baselineSerializer.deserialize(timestamp);
                    if (baseline != null)
                    {
                        patternBuilder.updateBaseline(baseline);
                    }
                    else
                    {
                        logger.warn("Command parameter is incorrect - cannot read baseline from given file\nCommand is being ignored.");
                    }
                }
                else if (buffer.startsWith("SET_LEVER"))
                {
                    buffer = StringUtils.removeStart(buffer, "SET_LEVER ");
                    String[] args = buffer.split(" ");
                    int percentLeverValue = Integer.parseInt(args[0]);
                    leverInfoHelper.setLeverValue(percentLeverValue);
                }
                else if (buffer.startsWith("SET_ANOMALY_LIVE_TIME"))
                {
                    buffer = StringUtils.removeStart(buffer, "SET_ANOMALY_LIVE_TIME ");
                    String[] args = buffer.split(" ");
                    int secondsAnomalyLiveTime = Integer.parseInt(args[0]);
                    anomalyLiveTimeInfoHelper.setAnomalyLiveTimeValue(secondsAnomalyLiveTime);
                }
                else if (buffer.startsWith("SET_BASELINE_WINDOW_SIZE"))
                {
                    buffer = StringUtils.removeStart(buffer, "SET_BASELINE_WINDOW_SIZE ");
                    String[] args = buffer.split(" ");
                    int baselineWindowSize = Integer.parseInt(args[0]);
                    baselineWindowSizeInfoHelper.setBaselineWindowSizeValue(baselineWindowSize);
                }
                else if (buffer.startsWith("RESET_PREFERENCES"))
                {
                    boolean result = options.resetPreferences();
                    logger.info("Preferences reset - " + result);
                    System.out.println("Preferences reset - " + result);
                }
                else if (buffer.startsWith("AV_H"))
                {
                    System.out.println(AvailableHistoricalInfoHelper.getAvailableDateRoutes().keySet());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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