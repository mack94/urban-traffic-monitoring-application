package pl.edu.agh.pp.charts.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.cs.*;
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
                new NioClient(InetAddress.getLocalHost(), 0, srv_addr, srv_port) :
                new TcpClient(InetAddress.getLocalHost(), 0, srv_addr, srv_port);
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

    @Override
    public void receive(Address sender, ByteBuffer buf) {
        try {
            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(buf.array());
            Connector.onMessage(message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("ChannelReceiver :: InvalidProtocolBufferException: " + e
                    + "\n Buf Array: " + Arrays.toString(buf.array())
                    + "\n Message: " + buf);
        }
    }

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {
        String msg = new String(buf, offset, length);
        logger.info(String.format("# %s\n", msg));
    }

    @Override
    public void connectionClosed(Connection conn, String reason) {
        client.stop();
        running = false;
        Util.close(in);
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
        if (!running) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("ChannelReceiver: Sleeping thread error: " + e);
            }
        }
        return running && !((client == null) || !((Client) client).isConnected());
    }

}
