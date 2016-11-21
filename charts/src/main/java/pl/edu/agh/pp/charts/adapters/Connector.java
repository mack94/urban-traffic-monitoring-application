package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.ChartsController;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.data.server.*;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.net.InetAddress;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static boolean isFromConnecting = false;
    private static String address;
    private static String port;

    private static MainWindowController mainWindowController;
    private static ChartsController chartsController;
    private static ChannelReceiver client;
    private static ManagementChannelReceiver managementClient;
    private final static AnomalyManager anomalyManager = AnomalyManager.getInstance();


    public static void setMainWindowController(MainWindowController mwc) {
        mainWindowController = mwc;
    }
    public static void setChartsController(ChartsController cc) {
        chartsController = cc;
    }

    public static void onAnomalyMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        if (anomalyMessage.getIsActive()) {
            anomalyManager.addAnomaly(anomalyMessage);
        } else {
            logger.info("Received expiration of route {}, anomaly ID {}", anomalyMessage.getRouteIdx(), anomalyMessage.getAnomalyID());
            anomalyManager.removeAnomaly(String.valueOf(anomalyMessage.getAnomalyID()));
        }
    }

    public static void setIsFromConnecting(boolean is) {
        isFromConnecting = is;
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
    }

    public static String getAddress() {
        return address;
    }

    public static String getPort() {
        return port;
    }

    public static void disconnect() {
        client.disconnect();
        managementClient.disconnect();
    }

    public static String getAddressServerInfo() {
        return address + ":" + port;
    }

    public static boolean isConnectedToTheServer() {
        return client != null && client.isConnected();
    }

    public static void killAll() {
        if (client != null)
            client.killConnectionThread();
    }

    public static void updateServerInfo(double leverValue, int anomalyLiveTime, int baselineWindowSize, AnomalyOperationProtos.SystemGeneralMessage.Shift shift, int anomalyMessagesPort) {
        mainWindowController.updateServerInfo(leverValue, anomalyLiveTime, baselineWindowSize, shift, anomalyMessagesPort);
    }

    public static void updateBaseline(Integer routeID, AnomalyOperationProtos.BaselineMessage.Day day, Map<Integer, Integer> baseline, String type) {
        BaselineManager.addBaseline(routeID, DayOfWeek.valueOf(String.valueOf(day)), baseline, type);
    }

    public static void demandBaseline(DayOfWeek dayOfWeek, int routeID,String type) {
        //TODO add type to demand baseline
        AnomalyOperationProtos.DemandBaselineMessage demandBaselineMessage = AnomalyOperationProtos.DemandBaselineMessage.newBuilder()
                .setDay(AnomalyOperationProtos.DemandBaselineMessage.Day.forNumber(dayOfWeek.getValue()))
                .setRouteIdx(routeID)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.DEMANDBASELINEMESSAGE)
                .setDemandBaselineMessage(demandBaselineMessage)
                .build();
        try {
            byte[] toSend = managementMessage.toByteArray();
            managementClient.sendMessage(toSend, 0, toSend.length);
        } catch (Exception e) {
            logger.error("Exception while demanding baseline " + e, e);
        }
    }

    public static void demandAvailableHistorical() {
        AnomalyOperationProtos.DemandAvailableHistoricalMessage demandAvailableHistoricalMessage = AnomalyOperationProtos
                .DemandAvailableHistoricalMessage.newBuilder()
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos
                .ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.DEMANDAVAILABLEHISTORICALMESSAGE)
                .setDemandAvailableHistoricalMessage(demandAvailableHistoricalMessage)
                .build();
        try {
            byte[] toSend = managementMessage.toByteArray();
            managementClient.sendMessage(toSend, 0, toSend.length);
        } catch (Exception e) {
            logger.error("Exception while demanding baseline " + e, e);
        }
    }

    public static void updateAvailableDates(Map<String,List<Integer>> arg){
        System.out.println("###4 - ok");
        ServerDatesInfo.setMap(arg);
        System.out.println("MAP::: " + arg);
        setServerAvailableDates();
    }

    public static void updateHistoricalData(Integer routeID, DateTime date, Map<Integer, Integer> duration){
        HistoricalDataManager.addHistoricalData(routeID, date, duration);
    }

    public static void demandHistoricalData(DateTime date, int routeID){
        AnomalyOperationProtos.DemandHistoricalMessage demandHistoricalMessage = AnomalyOperationProtos.DemandHistoricalMessage.newBuilder()
                .setDate(date.toString("yyyy-MM-dd"))
                .setRouteID(routeID)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.DEMANDHISTORICALMESSAGE)
                .setDemandHistoricalMessage(demandHistoricalMessage)
                .build();
        try {
            byte[] toSend = managementMessage.toByteArray();
            managementClient.sendMessage(toSend, 0, toSend.length);
        } catch (Exception e) {
            logger.error("Exception while demanding historical data " + e, e);
        }

    }

    public static void updateHistoricalAnomalies(Integer routeID, DateTime date, Map<String,Map<Integer,Integer>> anomalies){
        HistoricalAnomalyManager.addHistoricalAnomalies(routeID, date, anomalies);
    }

    public static void demandHistoricalAnomalies(DateTime date, int routeID){
        //TODO @Maciek

    }

    public static void connectionLost(String additionalInfo) {
        if (mainWindowController != null) {
            String message = null;
            if (additionalInfo != null) {
                message = additionalInfo;
            }
            mainWindowController.setConnectedFlag();
            mainWindowController.setConnectedState();
            mainWindowController.putSystemMessageOnScreen(message, Color.RED);
            if (!isFromConnecting)
                mainWindowController.reconnecting();
        }
    }

    public static void setServerAvailableRouteIds(){
        if(chartsController != null) {
            chartsController.setServerRouteIds();
        }
    }

    public static void setServerAvailableDates(){
        if(chartsController != null) {
            chartsController.setServerDates();
        }
    }
}
