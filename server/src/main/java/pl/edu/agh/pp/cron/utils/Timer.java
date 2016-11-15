package pl.edu.agh.pp.cron.utils;

import ch.qos.logback.classic.Logger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Maciej on 15.05.2016.
 * 16:05
 * Project: 1.
 */
public class Timer {

    private final static Timer INSTANCE = new Timer();
    private static Logger logger = (Logger) LoggerFactory.getLogger(Timer.class.getClass());
    private final IOptions options = Options.getInstance();
    private static DayOfWeek dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());

    private Timer() {
    }

    public static Timer getInstance() {
        return INSTANCE;
    }

    public long getWaitingTime(Calendar currentCalendar) {
        try {
            String string1 = null;
            try {
                string1 = (String) options.getPreference("DayShiftStart", String.class);
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                illegalPreferenceObjectExpected.printStackTrace();
            }
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = null;
            try {
                string2 = (String) options.getPreference("NightShiftStart", String.class);
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                illegalPreferenceObjectExpected.printStackTrace();
            }
            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
//            calendar2.add(Calendar.DATE, 1);



            String hours = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY));
            String minutes = String.valueOf(currentCalendar.get(Calendar.MINUTE));
            String seconds = String.valueOf(currentCalendar.get(Calendar.SECOND));
            String currentTime = hours.concat(":").concat(minutes).concat(":").concat(seconds);
            Date d = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
            currentCalendar.setTime(d);
//            currentCalendar.add(Calendar.DATE, 1);

            Date x = currentCalendar.getTime();
            Random random = new Random();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                System.out.println("DAY SHIFT-----------------------------------------------");
                return random.nextInt(300_000) + 180_000;
            } else {
                System.out.println("NIGHT SHIFT----------------------------------------------");
                if (!Timer.dayOfWeek.equals(DayOfWeek.of(DateTime.now().getDayOfWeek()))) {
                    Timer.dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
                    System.out.println("New day. GC will run!");
                    System.gc();
                }
                return random.nextInt(600_000) + 250_000;
            }
        } catch (ParseException e) {
            logger.error("Error during calculating time to download traffic.");
        }
        return 720_000;
    }
}
