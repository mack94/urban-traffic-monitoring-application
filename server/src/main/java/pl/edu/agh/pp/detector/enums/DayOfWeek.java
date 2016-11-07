package pl.edu.agh.pp.detector.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Maciej on 19.07.2016.
 * 20:34
 * Project: detector.
 */
public enum DayOfWeek {
    NULLDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    private final static Logger logger = (Logger) LoggerFactory.getLogger(DayOfWeek.class);


    public static DayOfWeek fromValue(int value) throws IllegalArgumentException {
        try {
            return DayOfWeek.values()[value];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("DayOfWeek :: ArrayIndexOutOfBoundsException" + e);
            throw new IllegalArgumentException("Unknown enum value :" + (value));
        }
    }

}
