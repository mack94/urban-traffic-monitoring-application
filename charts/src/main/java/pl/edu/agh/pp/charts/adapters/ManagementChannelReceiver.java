package pl.edu.agh.pp.charts.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.blocks.cs.*;
import org.jgroups.util.ByteArrayDataInputStream;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.charts.system.SystemBaselineInfo;
import pl.edu.agh.pp.charts.system.SystemGeneralInfo;

import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Maciej on 30.10.2016.
 * 16:29
 * Project: charts.
 */
public class ManagementChannelReceiver extends ReceiverAdapter implements ConnectionListener {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ChannelReceiver.class);

    public String name = "ManagementCharts1";
    protected BaseServer client;
    protected InputStream in;
    protected volatile boolean running = true;
    protected Thread listenerThread;

    public void start(InetAddress srv_addr, int srv_port, boolean nio) throws Exception {
        // Initialize routes file
        File file = new File("./routes.json");
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.close();

        client = nio ?
                new NioClient(null, 0, srv_addr, srv_port) :
                new TcpClient(null, 0, srv_addr, srv_port);
        client.receiver(this);
        client.addConnectionListener(this);
        client.start();
        Thread.sleep(100);
        running = true;
        byte[] buf = String.format("%s joined\n", name).getBytes();
//        ((Client)client).send(buf, 0, buf.length);
//        eventLoop();
        listenerThread = new Thread(this::eventLoop);
        listenerThread.start();

        AnomalyOperationProtos.BonjourMessage bonjourMessage = AnomalyOperationProtos.BonjourMessage.newBuilder()
                .setUserName(name)
                .build();

        AnomalyOperationProtos.ManagementMessage managementMessage = AnomalyOperationProtos.ManagementMessage.newBuilder()
                .setBonjourMessage(bonjourMessage)
                .build();

        byte[] managementMessageToSent = managementMessage.toByteArray();
        ((Client) client).send(managementMessageToSent, 0, managementMessageToSent.length);
    }

    private void eventLoop() {

        in = new BufferedInputStream(System.in);
        Thread thisThread = Thread.currentThread();

//        while (running && listenerThread == thisThread) {
//            // TODO: Place where put stuff to send to server.
//            try {
//                byte[] buf = "".getBytes();
//                ((Client) client).send(buf, 0, buf.length);
//
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                logger.error("ChannelReceiver :: InterruptedException: " + e);
//            } catch (IOException e) {
//                logger.error("ChannelReceiver :: IOException: " + e);
//            } catch (Exception e) {
//                logger.error("ChannelReceiver :: Exception: " + e);
//                break;
//            }
//        }
    }

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {

        int bytesRead = 0;
        byte[] result = buf.clone();

//        logger.info("Management Message received");

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
                case SYSTEMGENERALMESSAGE:
                    parseGeneralMessage(message);
                    break;
                case ROUTEMESSAGE:
                    parseRouteMessage(message);
                    break;
                case BASELINEMESSAGE:
                    parseBaselineMessage(message);
                    break;
                case LEVERMESSAGE:
                    parseLeverMessage(message);
                    break;
                default:
                    logger.error("ManagementServer: Unknown management message type received.");
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("ManagementChannelReceiver: InvalidProtocolBufferException while parsing the received message. Error: " + e);
            logger.error("Following bytes received:");
            logger.error("\t\t" + Arrays.toString(buf));
        }

    }

    @Override
    public void connectionClosed(Connection conn, String reason) {
        client.stop();
        running = false;
        Util.close(in);
        Connector.connectionLost("https://scontent-fra3-1.xx.fbcdn.net/v/t1.0-9/14632931_1786457691626264_4938219509567281520_n.jpg?oh=e6bbac12573f62884b98a6ab077ce56b&oe=588D7696");
        logger.info(String.format("ManagementChannelReceiver :: Connection to %s closed: %s", conn.peerAddress(), reason));
        System.out.println("System status:" + this.isConnected());
        // Clear routes file
        File file = new File("./routes.json");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, false);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionEstablished(Connection conn) {
        logger.info("ManagementChannelReceiver :: Connection established");
        System.out.println("System status:" + this.isConnected());
    }

    public boolean isConnected() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error("ChannelReceiver: Sleeping thread error: " + e);
        }

        return running && !((client == null) || !((Client) client).isConnected());
    }

    private void parseGeneralMessage(AnomalyOperationProtos.ManagementMessage message) {
        try {
            AnomalyOperationProtos.SystemGeneralMessage generalMessage = AnomalyOperationProtos.SystemGeneralMessage.parseFrom(message.getSystemGeneralMessage().toByteArray());
            String routes = generalMessage.getRoutes();
            double leverValue = generalMessage.getLeverValue();
            SystemGeneralInfo.setSystemGeneralMessage(generalMessage);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void parseLeverMessage(AnomalyOperationProtos.ManagementMessage message) {
        try {
            AnomalyOperationProtos.LeverMessage leverMessage = AnomalyOperationProtos.LeverMessage.parseFrom(message.getLeverMessage().toByteArray());
            double leverValue = leverMessage.getLeverValue();
            SystemGeneralInfo.setLeverValue(leverValue);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void parseRouteMessage(AnomalyOperationProtos.ManagementMessage message) {
        try {
            AnomalyOperationProtos.RouteMessage routeMessage = AnomalyOperationProtos.RouteMessage.parseFrom(message.getRouteMessage().toByteArray());
            SystemGeneralInfo.addRoute(routeMessage);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void parseBaselineMessage(AnomalyOperationProtos.ManagementMessage message) {
        try {
            AnomalyOperationProtos.BaselineMessage baselineMessage = AnomalyOperationProtos.BaselineMessage.parseFrom(message.getBaselineMessage().toByteArray());
            int routeID = baselineMessage.getRouteIdx();
            AnomalyOperationProtos.BaselineMessage.Day day = baselineMessage.getDay();
            Map<Integer, Integer> baselineMap = baselineMessage.getBaselineMap();
            SystemBaselineInfo.addBaselineInfo(routeID, day, baselineMap);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    protected void sendMessage(byte[] toSend, int i, int length) throws Exception {
        ((Client) client).send(toSend, i, toSend.length);
    }
}
