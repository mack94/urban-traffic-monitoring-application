package pl.edu.agh.pp.charts.adapters.exceptions;

/**
 * Created by Maciej on 18.12.2016.
 * 22:21
 * Project: charts.
 */
public class ManagementChannelConnectionException extends Throwable {

    public ManagementChannelConnectionException() {
    }

    public ManagementChannelConnectionException(String message) {
        super(message);
    }

    public ManagementChannelConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagementChannelConnectionException(Throwable cause) {
        super(cause);
    }

    public ManagementChannelConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
