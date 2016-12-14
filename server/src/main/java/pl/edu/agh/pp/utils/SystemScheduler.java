package pl.edu.agh.pp.utils;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.detectors.DetectorManager;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
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
    private final ScheduledExecutorService generalMessageScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService baselineUpdateScheduler = Executors.newScheduledThreadPool(1);

    public void sendSystemGeneralMessageEveryHour() {
        final Runnable sender = () -> {
            try {
                Connector.updateSystem(null); // Send to all connected users.
            } catch (IOException e) {
                logger.error("SystemScheduler: IOException while sending general message every hour: " + e, e);
            }
        };

        int minutesToNewHour = 60 - DateTime.now().getMinuteOfHour();
        System.out.println("min" + minutesToNewHour);
        final ScheduledFuture<?> senderHandle = generalMessageScheduler
                .scheduleWithFixedDelay(sender, (minutesToNewHour * 60) + 60, 3600, TimeUnit.SECONDS);
    }

    public void updateBaselinesOnWeeklyBasis() {
        final Runnable baselineUpdater = () -> {
            try {
                DetectorManager.computeBaselineFromDefaultLogsLocation();
            } catch (IOException e) {
                logger.error("SystemScheduler: IOException while performing regular baseline update: " + e, e);
            }
        };

        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("Europe/Paris");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext5;
        zonedNext5 = zonedNow.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).withHour(14).withMinute(42).withSecond(0);
        if (zonedNow.compareTo(zonedNext5) > 0)
            zonedNext5 = zonedNext5.plusDays(1);

        Duration duration = Duration.between(zonedNow, zonedNext5);
        long initalDelay = duration.getSeconds();

        baselineUpdateScheduler.scheduleAtFixedRate(baselineUpdater, initalDelay,
                7 * 24 * 60 * 60, TimeUnit.SECONDS);
    }

}
