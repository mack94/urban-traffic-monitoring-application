package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.input.AnomalyManager;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.Properties;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static String address;
    private static String port;
    private static ChannelReceiver client;
    private static MainWindowController mainWindowController;
    private final static AnomalyManager anomalyManager = AnomalyManager.getInstance();

    public static void setMainWindowController(MainWindowController mwc){
        mainWindowController = mwc;
    }

    public static void onMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        anomalyManager.addAnomaly(anomalyMessage);
    }
    public static void connect(String addr, String prt) throws Exception {
        address = addr;
        port = prt;

        InetAddress server_addr = InetAddress.getByName(address);
        int server_port;
        server_port = Integer.valueOf(port);
        boolean nio = true;

        Properties properties = System.getProperties();
        properties.setProperty("jgroups.addr", server_addr.toString());

        client = new ChannelReceiver();
        client.start(server_addr, server_port, nio);
    }

    public static void disconnect(){
        client.disconnect();
    }

    public static String getAddressServerInfo(){
        return address + ":" + port;
    }

    public static boolean isConnectedToTheServer() {
        return client != null && client.isConnected();
    }

    public static void killAll(){
        if (client != null)
            client.killConnectionThread();
    }

    public static String getLeverServerInfo(){
        return "MAKOWA ZROB TO WKONCU";
    }

    public static void connectionLost(String additionalInfo) {
        if(mainWindowController != null){
            String message = LocalDate.now().toString() + " Lost connection to server! ";
            if(additionalInfo != null){
                message += additionalInfo;
            }
            mainWindowController.putSystemMessageOnScreen(message, Color.RED);
        }
    }
}
