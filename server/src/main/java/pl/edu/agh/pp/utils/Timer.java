package pl.edu.agh.pp.utils;

import ch.qos.logback.classic.Logger;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

/**
 * Created by Maciej on 15.05.2016.
 * 16:05
 * Project: 1.
 */
public class Timer
{

    private final static Timer INSTANCE = new Timer();
    private static Logger logger = (Logger) LoggerFactory.getLogger(Timer.class.getClass());
    private static DayOfWeek dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
    private int frequencyMin = 0;
    private int frequencyMax = 0;

    private Timer()
    {
    }

    public static Timer getInstance()
    {
        return INSTANCE;
    }

    public long getWaitingTime(Calendar currentCalendar)
    {
        try
        {
            String string1 = DayShiftStartInfoHelper.getInstance().getDayShiftStart();
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = NightShiftStartInfoHelper.getInstance().getNightShiftStart();

            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);

            String hours = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY));
            String minutes = String.valueOf(currentCalendar.get(Calendar.MINUTE));
            String seconds = String.valueOf(currentCalendar.get(Calendar.SECOND));
            String currentTime = hours.concat(":").concat(minutes).concat(":").concat(seconds);
            Date d = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
            currentCalendar.setTime(d);

            int from;
            int to;
            int diff;
            int waitingTime;
            Date x = currentCalendar.getTime();
            Random random = new Random();

            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime()))
            {
                from = DayRequestsFrequencyInfoHelper.getInstance().getMinimalTimeValue();
                frequencyMin = from;
                to = DayRequestsFrequencyInfoHelper.getInstance().getMaximalTimeValue();
                frequencyMax = to;
                diff = to - from;
                waitingTime = random.nextInt(diff) + from;
                logger.info("DAY MODE - waiting time is {} seconds", waitingTime);
                return waitingTime;
            }
            else
            {
                from = NightRequestsFrequencyInfoHelper.getInstance().getMinimalTimeValue();
                frequencyMin = from;
                to = NightRequestsFrequencyInfoHelper.getInstance().getMaximalTimeValue();
                frequencyMax = to;
                diff = to - from;
                waitingTime = random.nextInt(diff) + from;
                logger.info("NIGHT MODE- waiting time is {} seconds", waitingTime);
                if (!Timer.dayOfWeek.equals(DayOfWeek.of(DateTime.now().getDayOfWeek())))
                {
                    Timer.dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
                    logger.info("New day. GC will run!");
                    System.gc();
                }
                return waitingTime;
            }
        }
        catch (Exception e)
        {
            logger.error("Error during calculating time to download traffic, returning default value <720>", e);
        }

        frequencyMin = 720;
        frequencyMax = 720;

        return 720;
    }

    public int getFrequencyMin() {
        return frequencyMin;
    }

    public int getFrequencyMax() {
        return frequencyMax;
    }

    public String getRequestFrequency() {
        getWaitingTime(Calendar.getInstance());
        return String.format("%d - %d", getFrequencyMin()/60, getFrequencyMax()/60);
    }
}
