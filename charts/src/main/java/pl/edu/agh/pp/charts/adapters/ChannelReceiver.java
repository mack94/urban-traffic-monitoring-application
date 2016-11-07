package pl.edu.agh.pp.charts.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.cs.*;
import org.jgroups.util.ByteArrayDataInputStream;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Maciej on 05.09.2016.
 * 21:26
 * Project: detector.
 */
public class ChannelReceiver extends ReceiverAdapter implements ConnectionListener {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ChannelReceiver.class);

    public String name = "Charts1";
    protected BaseServer client;
    protected InputStream in;
    protected volatile boolean running = true;
    protected Thread listenerThread;
    private JChannel channel; // final

    public ChannelReceiver() {
    }

    public ChannelReceiver(JChannel channel) {
        this.channel = channel;
    }

    public void start(InetAddress srv_addr, int srv_port, boolean nio) throws Exception {
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

    /**
     * Receive method for the NIO working mode.
     * @param sender Address of the message sender.
     * @param buf ByteBuffer with the messages
     * @see Address
     * @see ReceiverAdapter
     * @see ConnectionListener
     * @see pl.edu.agh.pp.charts.operations.AnomalyOperationProtos.AnomalyMessage
     */
    @Override
    public void receive(Address sender, ByteBuffer buf) {
        // TODO: Check this.
        //buf.rewind();
        // It's for the NIO mode. It is not tested already - but i think that rewind and flip should be removed.
        Util.bufferToArray(sender, buf, this);
        buf.rewind();
        buf.flip();
//        try {
//            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(buf);
//            Connector.onMessage(message);
//        } catch (InvalidProtocolBufferException e) {
//            logger.error("ChannelReceiver :: InvalidProtocolBufferException: " + e
//                    + "\n Buf Array: " + Arrays.toString(buf.array())
//                    + "\n Message: " + buf);
//        }
    }

    /**
     * Receive method for the TCP working mode.
     * @param sender Address of the message sender.
     * @param buf Buffer with the messages
     * @param offset The message offset
     * @param length The length of received message
     * @see Address
     * @see ReceiverAdapter
     * @see ConnectionListener
     * @see pl.edu.agh.pp.charts.operations.AnomalyOperationProtos.AnomalyMessage
     */
    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {

        int bytesRead = 0;
        byte[] result = buf.clone();

        logger.info("Message received");

        if (length < 0) {
            logger.error("Length is less than 0!");
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
            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(result_parsable);
            Connector.onMessage(message);
            logger.info("\t Message parsing completed - success");
        } catch (InvalidProtocolBufferException e) {
            logger.error("ChannelReceiver: InvalidProtocolBufferException while parsing the received message. Error: " + e);
            logger.error("Following bytes received:");
            logger.error("\t\t" + Arrays.toString(buf));
        }

    }

    @Override
    public void connectionClosed(Connection conn, String reason) {
        client.stop();
        running = false;
        Util.close(in);
        Connector.connectionLost(reason);
        logger.info(String.format("ChannelReceiver :: Connection to %s closed: %s", conn.peerAddress(), reason));
        System.out.println("System status:" + this.isConnected());
    }

    @Override
    public void connectionEstablished(Connection conn) {
        logger.info("ChannelReceiver :: Connection established");
        System.out.println("System status:" + this.isConnected());
    }

    public void killConnectionThread() {
        listenerThread = null;
    }

    public void disconnect() {
        if (client != null) {
            client.removeConnectionListener(this);
            client.stop();
        }
//        if (anomaliesConnection != null && anomaliesConnection.isConnected() && anomaliesConnection.isOpen())
//            try {
//                anomaliesConnection.close();
//            } catch (IOException ex) {
//                logger.error("Error while disconnecting from Anomalies channel/connection: " + ex);
//                // TODO: I am not sure if to handle an exception or throw. Rozkmina needed.
//            }
    }

    public boolean isConnected() {
        return running && !((client == null) || !((Client) client).isConnected());
    }

}
