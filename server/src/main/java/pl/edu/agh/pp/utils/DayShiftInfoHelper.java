package pl.edu.agh.pp.utils;

import ch.qos.logback.classic.Logger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.utils.enums.DayShift;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Maciej on 14.11.2016.
 * 17:21
 * Project: server.
 */

public class DayShiftInfoHelper {

    private static Logger logger = (Logger) LoggerFactory.getLogger(DayShiftInfoHelper.class.getClass());
    private static DayOfWeek dayOfWeek = DayOfWeek.of(DateTime.now().getDayOfWeek());
    private static DayShift dayShift = DayShift.UNIVERSAL;
    private final IOptions options = Options.getInstance();

    public static DayShiftInfoHelper getInstance() {
        return Holder.INSTANCE;
    }

    private DayShift getShift() {

        Calendar currentCalendar = Calendar.getInstance();

        try {
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
        } catch (Exception e) {
            logger.error("Error occurred while getting shift", e);
        }

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

    private void updateSystemAfterChange() throws IOException {
        Connector.updateSystem(null); // To each user connected to the system;
    }

    public static class Holder {
        static final DayShiftInfoHelper INSTANCE = new DayShiftInfoHelper();
    }

}
