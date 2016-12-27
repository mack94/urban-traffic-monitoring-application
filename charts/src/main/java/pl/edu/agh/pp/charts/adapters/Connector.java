package pl.edu.agh.pp.charts.adapters;

import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.adapters.exceptions.ManagementChannelConnectionException;
import pl.edu.agh.pp.charts.adapters.exceptions.SystemGeneralInfoInitializationException;
import pl.edu.agh.pp.charts.controller.ChartsController;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.data.local.HtmlBuilder;
import pl.edu.agh.pp.charts.data.server.*;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.net.InetAddress;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private final static AnomalyManager anomalyManager = AnomalyManager.getInstance();
    private static int fromConnecting = 0;
    private static String address;
    private static String port;
    private static MainWindowController mainWindowController;
    private static ChartsController chartsController;
    private static ChannelReceiver client;
    private static ManagementChannelReceiver managementClient;

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
        if (is) fromConnecting++;
        else fromConnecting--;
    }

    public static boolean isFromConnecting() {
        if (fromConnecting > 0) return true;
        return false;
    }

    public static void connect(String addr, String prt) throws Exception {
        System.out.println("conn");
        address = addr;
        port = prt;

        InetAddress server_addr = InetAddress.getByName(address);
        int server_port;
        server_port = Integer.valueOf(port);
        boolean nio = true; // FIXME

        Properties properties = System.getProperties();
        properties.setProperty("jgroups.addr", server_addr.toString());

        managementClient = new ManagementChannelReceiver();
        managementClient.start(server_addr, server_port, nio);

        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int limit = 10;
                    while (!managementClient.isConnected() && limit > 0) {
                        logger.info("Waiting for management channel connection establishment.");
                        Thread.sleep((11 - limit) * 60);
                        limit--;
                    }

                    if (limit <= 0) {
                        throw new ManagementChannelConnectionException();
                    }

                    limit = 10;
                    while (!ServerGeneralInfo.isInitialized() && limit > 0) {
                        logger.info("Waiting for ServerGeneralInfo initialization");
                        Thread.sleep((11 - limit) * 60);
                        limit--;
                    }

                    if (limit <= 0) {
                        throw new SystemGeneralInfoInitializationException();
                    }

                    if (managementClient.isConnected()) {
                        int anomaly_port = ServerGeneralInfo.getPort();
                        client = new ChannelReceiver();
                        client.start(server_addr, anomaly_port, nio);
                    } else {
                        logger.error("Connector:: connect:: An error occurred while connecting to the management channel. ");
                    }
                } catch (ManagementChannelConnectionException e) {
                    logger.error("Error while reconnecting. Management Channel is probably not reachable." + e, e);
                    // TODO: Maybe some action?
                } catch (SystemGeneralInfoInitializationException e) {
                    logger.error("Error while reconnecting. Management Channel reachable but not response. " + e, e);
                    // TODO: Maybe some action?
                }
                return null;
            }
        };
        new Thread(sleeper).start();
    }

    public static String getAddress() {
        return address;
    }

    public static String getPort() {
        return port;
    }

    public static void disconnect() {
        if (client != null)
            client.disconnect();
        if (managementClient != null)
            managementClient.disconnect();
        if (anomalyManager != null)
            anomalyManager.clearAnomalies();
    }

    public static String getAddressServerInfo() {
        return address + ":" + port;
    }

    public static boolean isConnectedToTheServer() {
        return managementClient != null && managementClient.isConnected() && client != null && client.isConnected();
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

    public static void demandBaseline(DayOfWeek dayOfWeek, int routeID, String type) {
//        type = "2016-11-05_17-53-32";
        AnomalyOperationProtos.DemandBaselineMessage demandBaselineMessage = AnomalyOperationProtos.DemandBaselineMessage.newBuilder()
                .setDay(AnomalyOperationProtos.DemandBaselineMessage.Day.forNumber(dayOfWeek.getValue()))
                .setRouteIdx(routeID)
                .setBaselineType(type)
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

    public static void updateAvailableDates(Map<String, List<Integer>> arg) {
        ServerDatesInfo.setMap(arg);
        setServerAvailableDates();
    }

    public static void updateAvailableRoutes() {
        mainWindowController.setAvailableRoutes();
    }

    public static void updateHistoricalData(Integer routeID, DateTime date, Map<Integer, Integer> duration) {
        HistoricalDataManager.addHistoricalData(routeID, date, duration);
    }

    public static void demandHistoricalData(DateTime date, int routeID) {
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

    public static void updateHistoricalAnomalies(Integer routeID, DateTime date, Map<String, Map<Integer, Integer>> anomalies) {
        HistoricalAnomalyManager.addHistoricalAnomalies(routeID, date, anomalies);
    }

    public static void demandHistoricalAnomalies(DateTime date, int routeID) {
        AnomalyOperationProtos.DemandHistoricalAnomaliesMessage demandHistoricalAnomaliesMessage = AnomalyOperationProtos.DemandHistoricalAnomaliesMessage.newBuilder()
                .setDate(date.toString("yyyy-MM-dd"))
                .setRouteID(routeID)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.DEMANDHISTORICALANOMALIESMESSAGE)
                .setDemandHistoricalAnomaliesMessage(demandHistoricalAnomaliesMessage)
                .build();
        try {
            byte[] toSend = managementMessage.toByteArray();
            managementClient.sendMessage(toSend, 0, toSend.length);
        } catch (Exception e) {
            logger.error("Exception while demanding historical anomalies data " + e, e);
        }
    }

    public static void connectionLost(String additionalInfo) {
        anomalyManager.clearAnomalies();
        if (mainWindowController != null) {
            String message = null;
            if (additionalInfo != null) {
                message = additionalInfo;
            }
            mainWindowController.setConnectedFlag();
            mainWindowController.setConnectedState();
            mainWindowController.putSystemMessageOnScreen(message, Color.RED);
            if (!isFromConnecting())
                mainWindowController.reconnecting();
        }
    }

    public static void setServerAvailableRouteIds() {
        if (chartsController != null) {
            chartsController.setServerRouteIds();
        }
    }

    public static void setServerAvailableDates() {
        if (chartsController != null) {
            chartsController.setServerDates();
        }
    }

    public static void setMapsApiKey(String apiKey) throws IllegalPreferenceObjectExpected {
        HashMap<String, Object> map = new HashMap<>();
        map.put("MAPS_API_KEY", apiKey);
        Options.getInstance().setPreferences(map);
        HtmlBuilder.reloadApiKey();
    }

    public static void setCurrentAnomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        Connector.onAnomalyMessage(anomalyMessage);
    }
}
