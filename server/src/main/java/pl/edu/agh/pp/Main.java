package pl.edu.agh.pp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.detector.DetectorManager;
import pl.edu.agh.pp.detector.adapters.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Created by Jakub Janusz on 07.09.2016.
 * 20:18
 * server
 */
public class Main {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    private static boolean running_mode = true;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Run: 'java -jar server.jar on/off path_to_logs'");
        Thread.sleep(2000);

        if (Objects.equals(args[0], "on"))
            running_mode = true;
        else if (Objects.equals(args[0], "off"))
            running_mode = false;

        if (running_mode) {
            InetAddress bind_addr = null; // FIXME
            try {
                bind_addr = InetAddress.getByName("0.0.0.0");
                logger.info("Server listens on: " + bind_addr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            int port = 7500;
            boolean nio = true;

            logger.info("Running server in 5 sec.");
            Thread.sleep(5000);
            Server server = new Server();
            try {
                server.start(bind_addr, port, nio);
                logger.info("Server already running.");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(15000);
            new CronManager(server).doSomething(args[1]);
        } else {
            Server server = new Server();
            new DetectorManager(server, args[1]).displayAnomaliesForRoute(4);
        }
    }
}
