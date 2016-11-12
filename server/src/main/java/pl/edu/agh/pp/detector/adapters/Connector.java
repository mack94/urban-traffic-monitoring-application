package pl.edu.agh.pp.detector.adapters;

import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.cron.utils.ContextLoader;

import java.net.InetAddress;

/**
 * Created by Maciej on 09.11.2016.
 * 22:09
 * Project: server.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static ChannelReceiver channelReceiver = new ChannelReceiver();
    private static ManagementServer managementServer = new ManagementServer();
    private static Server server;

    public static void connect(String[] args, InetAddress bind_addr, int port, boolean nio) throws InterruptedException {
        managementServer = new ManagementServer();
        server = new Server();
        channelReceiver = new ChannelReceiver();

        try {
            // TODO: I am not sure if we should start both management and anomalies channel both at the same time.
            managementServer.start(bind_addr, port - 1, nio);
            server.start(bind_addr, port, nio);
            channelReceiver.start(bind_addr, port, nio);
            logger.info("Server already running.");
        } catch (Exception e) {
            logger.error("Connector :: Exception " + e, e);
        }

        Thread.sleep(10000);
        if(args.length>1)
            new CronManager(server).doSomething(args[1]);
        else
            new CronManager(server).doSomething("");
    }

    public static void updateLever(double leverValue) {
        managementServer.sendLeverInfoMessage(leverValue);
    }

}
