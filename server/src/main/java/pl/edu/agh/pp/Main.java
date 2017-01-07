package pl.edu.agh.pp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Connector;
import pl.edu.agh.pp.detectors.DetectorManager;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.prefs.BackingStoreException;

/**
 * Created by Jakub Janusz on 07.09.2016.
 * 20:18
 * server
 */
public class Main {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    private static boolean running_mode = true;
    private static IOptions options = Options.getInstance();

    public static void main(String[] args) throws Exception {

        System.out.println("Run: 'java -jar server.jar on/off path_to_logs'");
        try {
            options.initialize();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Objects.equals(args[0], "on"))
            running_mode = true;
        else if (Objects.equals(args[0], "off"))
            running_mode = false;

        if (running_mode) {
            InetAddress bind_addr = null;
            try {
                bind_addr = InetAddress.getByName("0.0.0.0");
                logger.info("AnomaliesServer listens on: " + bind_addr);
            } catch (UnknownHostException e) {
                logger.error("Main :: UnknownHostException " + e);
            }
            int port = 6098;
            boolean nio = false;

            logger.info("---------------------Running server.--------------------------");

            Connector.connect(args, bind_addr, port, nio);

        } else {
            logger.info("---------------------Offline mode.--------------------------");
            handleOfflineMode(args);

        }
    }

    private static void handleOfflineMode(String[] args) throws Exception {
        File logDir;
        if (args.length >= 5) {
            if (Objects.equals(args[1], "build")) {
                logDir = new File(args[args.length - 1]);

                if (logDir.isDirectory()) {
                    new DetectorManager(logDir.getAbsolutePath())
                            .buildAndShowBaseline(Integer.parseInt(args[2]), DayOfWeek.fromValue(Integer.parseInt(args[3])), args[4], Arrays.copyOfRange(args, 5, args.length));
                } else {
                    new DetectorManager("")
                            .buildAndShowBaseline(Integer.parseInt(args[2]), DayOfWeek.fromValue(Integer.parseInt(args[3])), args[4], Arrays.copyOfRange(args, 5, args.length));
                }
            } else {
                closeOfflineMode(true);
            }
        } else if (args.length >= 2) {
            if (Objects.equals(args[1], "reset")) {
                Options options = Options.getInstance();
                boolean result = options.resetPreferences();
                logger.info("Preferences reset - ", result);
                closeOfflineMode(false);
            }
            closeOfflineMode(true);
        } else {
            closeOfflineMode(true);
        }

    }

    private static void closeOfflineMode(boolean error) {
        if (error) logger.error("Run: 'java -jar anomaliesServer.jar off <command> <options>'");
        System.exit(0);
    }
}
