package pl.edu.agh.pp.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jgroups.Address;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.cs.BaseServer;
import org.jgroups.blocks.cs.NioServer;
import org.jgroups.blocks.cs.Receiver;
import org.jgroups.blocks.cs.TcpServer;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.ByteArrayDataInputStream;
import org.jgroups.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detectors.DetectorManager;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.serializers.FileBaselineSerializer;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.utils.*;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maciej on 30.10.2016.
 * 00:15
 * Project: server.
 */
public class ManagementServer extends ReceiverAdapter implements Receiver {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ManagementServer.class);
    protected BaseServer server;
    private IOptions options = Options.getInstance();

    public void start(InetAddress bind_addr, int port, boolean nio) throws Exception {
        server = nio ? new NioServer(bind_addr, port) : new TcpServer(bind_addr, port);
        server.receiver(this);
        server.start();
        JmxConfigurator.register(server, Util.getMBeanServer(), "pub:name=pub-management-server");
        int local_port = server.localAddress() instanceof IpAddress ? ((IpAddress) server.localAddress()).getPort() : 0;
        logger.info(String.format("\nManagement server listening at %s:%s\n", bind_addr != null ? bind_addr : "0.0.0.0", local_port));
    }

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {

        int bytesRead = 0;
        int routeID;
        String date;
        byte[] result = buf.clone();

        logger.info("Management message received");

        if (length < 0) {
            logger.error("Length is less then 0!");
        }

        ByteArrayDataInputStream source = new ByteArrayDataInputStream(buf, offset, length);

        while (length != 0 && (bytesRead = source.read(result, offset, length)) > 0) {
            offset += bytesRead;
            length -= bytesRead;
        }
        if (length != 0) {
            logger.error("Something went wrong! There are still some bytes in the buffer.");
        }

        byte[] result_parsable = Arrays.copyOfRange(result, 0, bytesRead);

        try {
            AnomalyOperationProtos.ManagementMessage message = AnomalyOperationProtos.ManagementMessage.parseFrom(result_parsable);
            logger.info("\t Management Message parsing completed - success");
            AnomalyOperationProtos.ManagementMessage.Type messageType = message.getType();
            switch (messageType) {
                case BONJOURMESSAGE:
                    // TODO: Check the message
                    sendSystemGeneralMessage(sender);
                    sendRoutesMessages(sender);
                    break;
                case DEMANDBASELINEMESSAGE:
                    BaselineDemand parsedMessage = parseDemandBaselineMessage(message);
                    routeID = parsedMessage.routeID;
                    AnomalyOperationProtos.DemandBaselineMessage.Day day = parsedMessage.day;
                    String baselineType = parsedMessage.baselineType;
                    sendBaselineMessage(sender, routeID, day, baselineType);
                    break;
                case DEMANDAVAILABLEHISTORICALMESSAGE:
                    sendAvailableHistoricalMessage(sender);
                    break;
                case DEMANDHISTORICALMESSAGE:
                    HistoricalDemand historicalDemand = parseDemandHistoricalMessage(message);
                    date = historicalDemand.date;
                    routeID = historicalDemand.routeID;
                    sendHistoricalMessage(sender, date, routeID);
                    break;
                case DEMANDHISTORICALANOMALIESMESSAGE:
                    HistoricalAnomaliesDemand historicalAnomaliesDemand = parseDemandHistoricalAnomaliesMessage(message);
                    date = historicalAnomaliesDemand.date;
                    routeID = historicalAnomaliesDemand.routeID;
                    Connector.updateHistoricalAnomalies(sender, date, routeID);
                    break;
                default:
                    logger.error("ManagementServer: Unknown management message type received.");
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("ManagementServer: InvalidProtocolBufferException while parsing the received message. " +
                    "Error: " + e);
            logger.error("Following bytes received:");
            logger.error("\t\t" + Arrays.toString(buf));
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            logger.error("ManagementServer: IllegalPreferenceObjectExpected while receiving message! "
                    + illegalPreferenceObjectExpected, illegalPreferenceObjectExpected);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("ManagementServer: IOException while receiving message! " + e, e);
        }

    }

    @Override
    public void receive(Address sender, ByteBuffer buf) {

    }

    public void sendSystemGeneralMessage(Address destination) throws IllegalPreferenceObjectExpected, IOException {

        int anomalyLiveTime = AnomalyLiveTimeInfoHelper.getInstance().getAnomalyLiveTimeValue();
        int baselineWindowSize = BaselineWindowSizeInfoHelper.getInstance().getBaselineWindowSizeValue();
        double leverValue = LeverInfoHelper.getInstance().getLeverValue();
        //int anomaliesChannelPort = (int) options.getPreference("AnomaliesChannelPort", Integer.class); // FIXME
        int messageID = 1; // FIXME
        RoutesLoader routesLoader = RoutesLoader.getInstance();
        AnomalyOperationProtos.SystemGeneralMessage.Shift shift = DayShiftInfoHelper.getInstance().getShiftProtos(); // FIXME

        AnomalyOperationProtos.SystemGeneralMessage msg = AnomalyOperationProtos.SystemGeneralMessage.newBuilder()
                .setAnomalyLiveTime(anomalyLiveTime)
                .setBaselineWindowSize(baselineWindowSize)
                .setLeverValue(leverValue)
                .setMessageIdx(messageID)
                .setPort(8079)
                .setRoutes("")
                .setShift(shift)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.SYSTEMGENERALMESSAGE)
                .setSystemGeneralMessage(msg)
                .build();

        byte[] messageToSent = managementMessage.toByteArray();

        try {
            logger.info(server.printConnections());
            server.send(destination, messageToSent, 0, messageToSent.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRoutesMessages(Address destination) {
        RoutesLoader routesLoader = RoutesLoader.getInstance();
        JSONArray loadedRoutes = null;
        try {
            loadedRoutes = routesLoader.loadJSON();
            int loadedRoutesAmount = loadedRoutes.length();

            for (int i = 0; i < loadedRoutesAmount; i++) {
                JSONObject route = loadedRoutes.getJSONObject(i);
                String destinations[] = new String[1];
                String origins[] = new String[1];
                String coords[] = new String[1];
                String id = route.get("id").toString();
                destinations[0] = route.get("destination").toString();
                origins[0] = route.get("origin").toString();
                coords[0] = route.get("coords").toString();

                AnomalyOperationProtos.RouteMessage routeMessage = AnomalyOperationProtos.RouteMessage.newBuilder()
                        .setRouteID(Integer.parseInt(id))
                        .setOrigin(origins[0])
                        .setDestination(destinations[0])
                        .setCoords(coords[0])
                        .build();
                AnomalyOperationProtos.ManagementMessage msg = AnomalyOperationProtos.ManagementMessage.newBuilder()
                        .setType(AnomalyOperationProtos.ManagementMessage.Type.ROUTEMESSAGE)
                        .setRouteMessage(routeMessage)
                        .build();

                byte[] toSend = msg.toByteArray();
                Thread.sleep(250);
                server.send(destination, toSend, 0, toSend.length);
            }
        } catch (IOException e) {
            logger.error("ManagementServer: IOException while parsing available routes! " + e, e);
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while parsing available routes! " + e, e);
        }
    }


    public void sendLeverInfoMessage(double leverValue) {

        String leverUpdateDate = ""; // FIXME

        AnomalyOperationProtos.LeverMessage msg = AnomalyOperationProtos.LeverMessage.newBuilder()
                .setLeverValue(leverValue)
                .setLeverUpdateDate(leverUpdateDate)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.LEVERMESSAGE)
                .setLeverMessage(msg)
                .build();

        byte[] messageToSent = managementMessage.toByteArray();

        try {
            server.send(null, messageToSent, 0, messageToSent.length);
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while sending lever info! " + e, e);
        }
    }

    private void sendAvailableHistoricalMessage(Address destination) {

        AnomalyOperationProtos.AvailableHistoricalMessage availableHistoricalMessage = AvailableHistoricalInfoHelper
                .getAvailableHistoricalMessage();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.AVAILABLEHISTORICALMESSAGE)
                .setAvailableHistoricalMessage(availableHistoricalMessage)
                .build();

        byte[] messageToSent = managementMessage.toByteArray();

        try {
            server.send(destination, messageToSent, 0, messageToSent.length);
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while sending available historical message! " + e, e);
        }
    }

    private void sendHistoricalMessage(Address destination, String date, int routeID) {

        AnomalyOperationProtos.HistoricalMessage historicalMessage = HistoricalInfoHelper
                .getHistoricalMessage(date, routeID);

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.HISTORICALMESSAGE)
                .setHistoricalMessage(historicalMessage)
                .build();

        byte[] messageToSent = managementMessage.toByteArray();

        try {
            System.out.println("Send Historical message to: " + destination);
            server.send(destination, messageToSent, 0, messageToSent.length);
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while sending historical message! " + e, e);
        }
    }

    private void sendBaselineMessage(Address destination, int routeID, AnomalyOperationProtos.DemandBaselineMessage.Day day, String baselineType) {
        //TODO: Check if routeID is not -1
        //TODO: Be careful about sending message too fast - if you send it too fast, when PolynomialPatternBuilder is not loaded, then message will not be send.
        int dayNumber = day.getNumber();
        DayOfWeek dayOfWeek = DayOfWeek.fromValue(dayNumber);
        Map<Integer, Integer> baselineMap = new HashMap<>();

        if (baselineType != null && baselineType.length() > 0) {
            logger.info("It's from deserialization.");
            Map<DayOfWeek, Map<Integer, PolynomialFunction>> fbs = FileBaselineSerializer.getInstance().deserialize(baselineType);
            PolynomialFunction pf = fbs.get(dayOfWeek).get(routeID);
            int second = 0;
            while (second < 86400) {
                baselineMap.put(second, (int) pf.value(second));
                second += 60;
            }
        } else {
            logger.info("It's from current baseline.");
            double[] values = PolynomialPatternBuilder.getValueForEachMinuteOfDay(dayOfWeek, routeID);
            int second = 0;
            for (double value : values) {
                baselineMap.put(second, (int) value);
                second += 60;
            }
        }

        AnomalyOperationProtos.BaselineMessage baselineMessage = AnomalyOperationProtos.BaselineMessage.newBuilder()
                .setRouteIdx(routeID)
                .putAllBaseline(baselineMap)
                .setDay(AnomalyOperationProtos.BaselineMessage.Day.forNumber(dayNumber))
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setType(AnomalyOperationProtos.ManagementMessage.Type.BASELINEMESSAGE)
                .setBaselineMessage(baselineMessage)
                .build();

        try {
            byte[] toSend = managementMessage.toByteArray();
            server.send(destination, toSend, 0, toSend.length);
            logger.info("Baseline sent");
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while sending baseline! " + e, e);
        }
    }


    protected void sendHistoricalAnomaliesMessage(Address destination, String date, int routeID, Server dmServer) {

        try {
            Map<String, AnomalyOperationProtos.HistoricalAnomalyPresenceMessage> result = new HashMap<>();

            DetectorManager detectorManager = new DetectorManager(dmServer, true);
            Map<String, Map<Integer, Integer>> anomalyForDateAndRoute = detectorManager
                    .getAnomalyForDateAndRoute(date, routeID);

            for (String anomalyID: anomalyForDateAndRoute.keySet()) {
                AnomalyOperationProtos.HistoricalAnomalyPresenceMessage historicalAnomalyValue = AnomalyOperationProtos
                        .HistoricalAnomalyPresenceMessage.newBuilder()
                        .putAllPresence(anomalyForDateAndRoute.get(anomalyID))
                        .build();
                result.put(anomalyID, historicalAnomalyValue);
            }

            AnomalyOperationProtos.HistoricalAnomaliesMessage historicalAnomaliesMessage = AnomalyOperationProtos
                    .HistoricalAnomaliesMessage.newBuilder()
                    .setDate(date)
                    .setRouteID(routeID)
                    .putAllAnomalies(result)
                    .build();

            AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos
                    .ManagementMessage.newBuilder()
                    .setType(AnomalyOperationProtos.ManagementMessage.Type.HISTORICALANOMALIESMESSAGE)
                    .setHistoricalAnomaliesMessage(historicalAnomaliesMessage)
                    .build();

            byte[] toSend = managementMessage.toByteArray();
            server.send(destination, toSend, 0, toSend.length);
            logger.info("Historical anomalies sent");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("ManagementServer: Exception while sending historical anomalies! " + e, e);
        }
    }

    private BaselineDemand parseDemandBaselineMessage(AnomalyOperationProtos.ManagementMessage message) {

        BaselineDemand result = new BaselineDemand();

        try {
            AnomalyOperationProtos.DemandBaselineMessage demandBaselineMessage = AnomalyOperationProtos.DemandBaselineMessage.parseFrom(message.getDemandBaselineMessage().toByteArray());
            logger.info("Demand baseline for ID=" + demandBaselineMessage.getRouteIdx() + " day: " + demandBaselineMessage.getDay());
            Integer routeID = demandBaselineMessage.getRouteIdx();
            AnomalyOperationProtos.DemandBaselineMessage.Day day = demandBaselineMessage.getDay();
            String baselineType = demandBaselineMessage.getBaselineType();
            result.routeID = routeID;
            result.day = day;
            result.baselineType = baselineType;
        } catch (InvalidProtocolBufferException e) {
            logger.error("ManagementServer: Exception while parsing demand baseline message! " + e, e);
        }
        return result;
    }

    private HistoricalDemand parseDemandHistoricalMessage(AnomalyOperationProtos.ManagementMessage message) {

        HistoricalDemand result = new HistoricalDemand();

        try {
            AnomalyOperationProtos.DemandHistoricalMessage demandHistoricalMessage = AnomalyOperationProtos
                    .DemandHistoricalMessage.parseFrom(message.getDemandHistoricalMessage().toByteArray());
            logger.info("Demand historical data for ID=" + demandHistoricalMessage.getRouteID()
                    + " day: " + demandHistoricalMessage.getDate());
            Integer routeID = demandHistoricalMessage.getRouteID();
            String date = demandHistoricalMessage.getDate();
            result.routeID = routeID;
            result.date = date;
        } catch (InvalidProtocolBufferException e) {
            logger.error("ManagementServer: Exception while parsing demand historical message! " + e, e);
        }

        return result;
    }

    private HistoricalAnomaliesDemand parseDemandHistoricalAnomaliesMessage(AnomalyOperationProtos.ManagementMessage message) {

        HistoricalAnomaliesDemand result = new HistoricalAnomaliesDemand();

        try {
            AnomalyOperationProtos.DemandHistoricalAnomaliesMessage demandHistoricalAnomaliesMessage = AnomalyOperationProtos
                    .DemandHistoricalAnomaliesMessage.parseFrom(message.getDemandHistoricalAnomaliesMessage().toByteArray());

            logger.info("Demand historical anomalies data for ID=" + demandHistoricalAnomaliesMessage.getRouteID()
                    + " day: " + demandHistoricalAnomaliesMessage.getDate());
            Integer routeID = demandHistoricalAnomaliesMessage.getRouteID();
            String date = demandHistoricalAnomaliesMessage.getDate();
            result.routeID = routeID;
            result.date = date;
        } catch (InvalidProtocolBufferException e) {
            logger.error("ManagementServer: Exception while parsing demand historical anomalies message! " + e, e);
        }

        return result;
    }

    private static class BaselineDemand {
        Integer routeID;
        AnomalyOperationProtos.DemandBaselineMessage.Day day;
        String baselineType;
    }

    private static class HistoricalDemand {
        Integer routeID;
        String date;
    }

    private static class HistoricalAnomaliesDemand {
        Integer routeID;
        String date;
    }
}
