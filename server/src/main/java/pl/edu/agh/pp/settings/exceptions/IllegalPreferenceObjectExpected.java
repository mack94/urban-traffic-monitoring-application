package pl.edu.agh.pp.settings.exceptions;

/**
 * Created by Maciej on 08.10.2016.
 *
 * @author Maciej Makówka
 *         15:29
 *         Project: server.
 */
public class IllegalPreferenceObjectExpected extends Exception {
    public IllegalPreferenceObjectExpected() {
    }

    public IllegalPreferenceObjectExpected(String message) {
        super(message);
    }

    public IllegalPreferenceObjectExpected(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalPreferenceObjectExpected(Throwable cause) {
        super(cause);
    }

    public IllegalPreferenceObjectExpected(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
