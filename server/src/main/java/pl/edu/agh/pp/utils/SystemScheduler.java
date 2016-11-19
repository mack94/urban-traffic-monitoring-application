package pl.edu.agh.pp.utils;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Maciej on 15.11.2016.
 * 01:46
 * Project: server.
 */
public class SystemScheduler {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(LeverInfoHelper.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void sendSystemGeneralMessageEveryHour() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                try {
                    Connector.updateSystem(null); // Send to all connected users.
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                    illegalPreferenceObjectExpected.printStackTrace();
                }
            }
        };

        int minutesToNewHour = 60 - DateTime.now().getMinuteOfHour();
        System.out.println("min" + minutesToNewHour);
        final ScheduledFuture<?> senderHandle = scheduler
                .scheduleWithFixedDelay(sender, (minutesToNewHour * 60) + 60, 3600, TimeUnit.SECONDS);
    }

}
