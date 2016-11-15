package pl.edu.agh.pp.detector.helpers;

import ch.qos.logback.classic.Logger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.adapters.Connector;
import pl.edu.agh.pp.detector.enums.DayShift;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Maciej on 14.11.2016.
 * 17:21
 * Project: server.
 */

// There is one known issue. The dependency between methods getting Shift from the place where the SystemGeneralMessage
// is being send and with this Connect.updateSystemGeneral ... but i want to have the shift updated on charts whenever
// somebody will ask about shift or when the Scheduler run though this helper.

public class DayShiftInfoHelper {

    private static Logger logger = (Logger) LoggerFactory.getLogger(DayShiftInfoHelper.class.getClass());
    private final IOptions options = Options.getInstance();
    private static DayOfWeek dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
    private static DayShift dayShift = DayShift.UNIVERSAL;

    public static DayShiftInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    // Be careful. This code is redundant with cron Timer.
    // Please consider whether it could be merged or not. (I mean especially the UML diagram and program structure)
    // I don't want to make a spaghetti...

    private DayShift getShift() {

        // TODO: Please add UNIVERSAL <?>
        // But maybe universal will be removed...

        Calendar currentCalendar = Calendar.getInstance();

        try {
            String string1 = (String) options.getPreference("DayShiftStart", String.class);

            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = (String) options.getPreference("NightShiftStart", String.class);

            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);

            String hours = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY));
            String minutes = String.valueOf(currentCalendar.get(Calendar.MINUTE));
            String seconds = String.valueOf(currentCalendar.get(Calendar.SECOND));
            String currentTime = hours.concat(":").concat(minutes).concat(":").concat(seconds);
            Date d = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
            currentCalendar.setTime(d);

            Date x = currentCalendar.getTime();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {

                if (dayShift.compareTo(DayShift.DAY) != 0) {
                    dayShift = DayShift.DAY;
                    updateSystemAfterChange();
                }

                return DayShift.DAY;

            } else {

                if (!DayShiftInfoHelper.dayOfWeek.equals(DayOfWeek.of(DateTime.now().getDayOfWeek())))
                    DayShiftInfoHelper.dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
                if (dayShift.compareTo(DayShift.NIGHT) != 0) {
                    dayShift = DayShift.NIGHT;
                    updateSystemAfterChange();
                }

                return DayShift.NIGHT;
            }
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            illegalPreferenceObjectExpected.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // It's an error i think. Not desired state.
        dayShift = DayShift.UNIVERSAL;
        return DayShift.NULLSHIFT;
    }

    public AnomalyOperationProtos.SystemGeneralMessage.Shift getShiftProtos() {
        getInstance().getShift();
        if (dayShift.compareTo(DayShift.DAY) == 0)
            return AnomalyOperationProtos.SystemGeneralMessage.Shift.DAY;
        if (dayShift.compareTo(DayShift.NIGHT) == 0)
            return AnomalyOperationProtos.SystemGeneralMessage.Shift.NIGHT;
        return AnomalyOperationProtos.SystemGeneralMessage.Shift.UNIVERSAL;
    }

    public static class Holder {
        static final DayShiftInfoHelper INSTANCE = new DayShiftInfoHelper();
    }

    private void updateSystemAfterChange() {
        try {
            Connector.updateSystem(null); // To each user connected to the system;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            illegalPreferenceObjectExpected.printStackTrace();
        }
    }

}
