package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.net.InetAddress;
import java.util.Properties;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static MainWindowController controller = null;
    private static String address;
    private static String port;
    private static ChannelReceiver client;

    public static void setController(MainWindowController mainWindowController) {
        controller = mainWindowController;
    }

    public static void onMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        if (controller != null) {
            int id = anomalyMessage.getRouteIdx();
            String message = anomalyMessage.getMessage() + " _ " + anomalyMessage.getAnomalyID() + " _ " + " _ date: " + anomalyMessage.getDate();
            int duration = anomalyMessage.getDuration();
            Color color = Color.CRIMSON;
            controller.putAnomalyMessageonScreen(id, message, anomalyMessage.getDate(), duration, color);
        }
    }
    public static void connect(String addr, String prt) throws Exception {
        address = addr;
        port = prt;

        InetAddress server_addr = InetAddress.getByName(address);
        int server_port;
        server_port = Integer.valueOf(port);
        boolean nio = true;

        Properties properties = System.getProperties();
        properties.setProperty("jgroups.bind_addr", server_addr.toString());

        client = new ChannelReceiver();
        client.start(server_addr, server_port, nio);
    }

    public static String getAddressServerInfo(){
        return address + ":" + port;
    }

    public static void onLeverChange(String value){
        logger.info("Chnging lever to: " + value);
    }

    public static boolean isConnectedToTheServer() {
        return client.isConnected();
    }

}
