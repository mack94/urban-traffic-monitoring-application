package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.input.AnomalyManager;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.ServerOptions;

import java.net.InetAddress;
import java.util.Properties;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static String address;
    private static String port;
    private static ManagementChannelReceiver managementClient;
    private static ChannelReceiver client;
    private static MainWindowController mainWindowController;
    private final static AnomalyManager anomalyManager = AnomalyManager.getInstance();
    private static double leverValue = 0.0;

    public static void setMainWindowController(MainWindowController mwc){
        mainWindowController = mwc;
    }

    public static void onMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        if(anomalyMessage.getIsActive()) {
            anomalyManager.addAnomaly(anomalyMessage);
        }
        else {
            // TODO: Dawid please handle it
            logger.info("Received expiration of route {}, anomaly ID {}", anomalyMessage.getRouteIdx(), anomalyMessage.getAnomalyID());
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
        properties.setProperty("jgroups.addr", server_addr.toString());

        managementClient = new ManagementChannelReceiver();
        managementClient.start(server_addr, server_port - 1, nio);
        client = new ChannelReceiver();
        client.start(server_addr, server_port, nio);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error("Connector: Sleeping thread error: " + e);
        }
    }

    public static void disconnect() {
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

    public static void getOptionsServerInfo(){
        //TODO send message to server asking for options
        if(isConnectedToTheServer()){

        }
    }

    public static void updateServerInfo(double leverValue, int anomalyLiveTime, int baselineWindowSize, AnomalyOperationProtos.SystemGeneralMessage.Shift shift, int anomalyMessagesPort){
        //TODO use this method after receiving options info from server
        // FIXME: In my opinion it should be moved into the SystemGeneralInfo class, and here only the getServerOptions should be called.
        // FIXME: But I obediently filled the form. ~Maciek
        ServerOptions serverOptions = Options.getInstance().getServerOptions();
        serverOptions.setLeverValue(String.valueOf(leverValue));
        serverOptions.setAnomalyLiveTime(String.valueOf(anomalyLiveTime));
        serverOptions.setBaselineWindowSize(String.valueOf(baselineWindowSize));
        serverOptions.setShift(String.valueOf(shift));
        serverOptions.setAnomalyPortNr(String.valueOf(anomalyMessagesPort));
        mainWindowController.updateServerInfo(serverOptions);
    }

    public static void connectionLost(String additionalInfo) {
        if(mainWindowController != null){
            String message = null;
            if(additionalInfo != null){
                message = additionalInfo;
            }
            mainWindowController.setConnectedFlag();
            mainWindowController.putSystemMessageOnScreen(message, Color.RED);
        }
    }
}
