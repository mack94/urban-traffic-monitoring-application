package pl.edu.agh.pp.charts.adapters.exceptions;

/**
 * Created by Maciej on 18.12.2016.
 * 22:21
 * Project: charts.
 */
public class SystemGeneralInfoInitializationException extends Throwable {

    public SystemGeneralInfoInitializationException() {
    }

    public SystemGeneralInfoInitializationException(String message) {
        super(message);
    }

    public SystemGeneralInfoInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemGeneralInfoInitializationException(Throwable cause) {
        super(cause);
    }

    public SystemGeneralInfoInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
