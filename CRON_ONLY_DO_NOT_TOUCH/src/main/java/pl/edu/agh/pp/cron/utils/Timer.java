package pl.edu.agh.pp.cron.utils;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private Timer() {
    }

    public static Timer getInstance() {
        return INSTANCE;
    }

    public long getWaitingTime() {
        try {
            String string1 = "05:00:00";
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = "23:00:00";
            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);


            Calendar currentCalendar = Calendar.getInstance();
            String hours = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY));
            String minutes = String.valueOf(currentCalendar.get(Calendar.MINUTE));
            String seconds = String.valueOf(currentCalendar.get(Calendar.SECOND));
            String currentTime = hours.concat(":").concat(minutes).concat(":").concat(seconds);
            Date d = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
            currentCalendar.setTime(d);
            currentCalendar.add(Calendar.DATE, 1);

            Date x = currentCalendar.getTime();
            Random random = new Random();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                //checks whether the current time is between 05:00:00 and 23:00:00.
                return random.nextInt(200_000) + 15_000;
            } else {
                System.out.println("YOOOOOOOOOOOOOO");
                return random.nextInt(600_000) + 30_000;
            }
        } catch (ParseException e) {
            logger.error("Error during calculating time to download traffic.");
        }
        return 720_000;
    }
}
