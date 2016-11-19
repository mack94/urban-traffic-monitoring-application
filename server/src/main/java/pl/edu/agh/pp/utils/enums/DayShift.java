package pl.edu.agh.pp.utils.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Maciej on 14.11.2016.
 * 17:28
 * Project: server.
 */
public enum DayShift {
    NULLSHIFT,
    DAY,
    NIGHT,
    UNIVERSAL;

    private final static Logger logger = (Logger) LoggerFactory.getLogger(DayShift.class);

    public static DayShift fromValue(int value) throws IllegalArgumentException {
        try {
            return DayShift.values()[value];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("DayShift :: ArrayIndexOutOfBoundsException" + e);
            throw new IllegalArgumentException("Unknown enum value :" + (value));
        }
    }

}
