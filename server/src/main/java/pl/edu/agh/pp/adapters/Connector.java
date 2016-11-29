package pl.edu.agh.pp.adapters;

import org.jgroups.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.utils.SystemScheduler;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Maciej on 09.11.2016.
 * 22:09
 * Project: server.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static ManagementServer managementServer;
    private static AnomaliesServer anomaliesServer;
    private static SystemScheduler systemScheduler;

    public static void connect(String[] args, InetAddress bind_addr, int port, boolean nio) throws InterruptedException {
        managementServer = new ManagementServer();
        anomaliesServer = new AnomaliesServer();

        try {
            // TODO: I am not sure if we should start both management and anomalies channel both at the same time.
            managementServer.start(bind_addr, port - 1, nio, "management");
            anomaliesServer.start(bind_addr, port, nio, "anomalies");
            logger.info("AnomaliesServer already running.");
        } catch (Exception e) {
            logger.error("Connector :: Exception " + e, e);
        }

        Thread.sleep(1000);

        systemScheduler = new SystemScheduler();
        systemScheduler.sendSystemGeneralMessageEveryHour();

        if (args.length > 1)
            new CronManager(anomaliesServer).doSomething(args[1]);
        else
            new CronManager(anomaliesServer).doSomething("");
    }

    public static void updateLever(double leverValue) {
        managementServer.sendLeverInfoMessage(leverValue);
    }

    public static void updateSystem(Address destination) throws IOException, IllegalPreferenceObjectExpected {
        managementServer.sendSystemGeneralMessage(destination);
    }

    public static void updateHistoricalAnomalies(Address destination, String date, int routeID) {
        managementServer.sendHistoricalAnomaliesMessage(destination, date, routeID, anomaliesServer);
    }

}
