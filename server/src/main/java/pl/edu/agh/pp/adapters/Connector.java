package pl.edu.agh.pp.adapters;

import org.jgroups.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.utils.SystemGeneralInfoHelper;
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
            managementServer.start(bind_addr, port, nio, "management");
            logger.info("ManagementServer already running.");
        } catch (Exception e) {
            logger.error("Connector :: Exception " + e, e);
        }
        int anomaly_port = SystemGeneralInfoHelper.getInstance().getAnomalyChannelPort();
        boolean success = false;
        while (!success) {
            try {
                anomaliesServer.start(bind_addr, anomaly_port, nio, "anomalies");
                logger.info("AnomaliesServer already running.");
                success = true;
            } catch (Exception e) {
                SystemGeneralInfoHelper.getInstance().setAnomalyChannelPort(anomaly_port - 1);
                anomaly_port = SystemGeneralInfoHelper.getInstance().getAnomalyChannelPort();
            }
        }

        Thread.sleep(1000);

        systemScheduler = new SystemScheduler();
        systemScheduler.sendSystemGeneralMessageEveryHour();
        systemScheduler.updateBaselinesOnWeeklyBasis();
//        systemScheduler.clearCurrentAnomaliesEveryAnomalyLifeTime();

        if (args.length > 1)
            new CronManager(anomaliesServer).getAPIData(args[1]);
        else
            new CronManager(anomaliesServer).getAPIData("");
    }

    public static void updateLever(double leverValue) {
        managementServer.sendLeverInfoMessage(leverValue);
    }

    public static void updateSystem(Address destination) throws IOException {
        managementServer.sendSystemGeneralMessage(destination);
    }

    public static void updateHistoricalAnomalies(Address destination, String date, int routeID) {
        managementServer.sendHistoricalAnomaliesMessage(destination, date, routeID, anomaliesServer);
    }

    public static void sendCurrentAnomaliesList(Address destination) {

    }

}
