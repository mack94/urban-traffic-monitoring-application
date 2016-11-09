package pl.edu.agh.pp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.detector.DetectorManager;
import pl.edu.agh.pp.detector.adapters.ChannelReceiver;
import pl.edu.agh.pp.detector.adapters.Connector;
import pl.edu.agh.pp.detector.adapters.ManagementServer;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;

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

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Run: 'java -jar server.jar on/off path_to_logs'");

        Thread.sleep(2000);
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
                logger.info("Server listens on: " + bind_addr);
            } catch (UnknownHostException e) {
                logger.error("Main :: UnknownHostException " + e);
            }
            int port = 7500;
            boolean nio = false;

            logger.info("Running server in 2 sec.");
            Thread.sleep(2000);

            Connector.connect(args, bind_addr, port, nio);
            //TODO:Uwaga! Usunąłem metody, które tutaj były (stawianie serwera i kanałów) i przeniosłem do Connectora.
            // Jak są jakieś błędy to proszę zgłaszać
            // Ponadto nie wiem czy działa teraz to co jest po else.
            // Bo brakuje testów i sobie nie sprawdzę :-/
            // ~Maciek

        } else {
            Server server = new Server();
            //new DetectorManager(server, args[1]).displayAnomaliesForRoute(1);
            if(args.length>1)
                new DetectorManager(server, Arrays.copyOfRange(args, 1, args.length)).displayAnomaliesForFile();
            else
                logger.error("Run: 'java -jar server.jar off path_to_logs'");
        }
    }
}
