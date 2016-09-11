package pl.edu.agh.pp.charts.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.cs.*;
import org.jgroups.util.Util;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Maciej on 05.09.2016.
 * 21:26
 * Project: detector.
 */
public class ChannelReceiver extends ReceiverAdapter implements ConnectionListener {

    public String name = "Charts1";
    protected BaseServer client;
    protected InputStream in;
    protected volatile boolean running = true;
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
        byte[] buf = String.format("%s joined\n", name).getBytes();
//        ((Client)client).send(buf, 0, buf.length);
//        eventLoop();
        new Thread(this::eventLoop).start();
//        client.stop();
    }

    private void eventLoop() {
        in = new BufferedInputStream(System.in);

        while (running) {
            // TODO: Place where put stuff to send to server.
            try {
                System.out.print("$ ");
                System.out.flush();
                String line = Util.readLine(in);
                if (line == null)
                    break;
                if (line.startsWith("emergency_call"))
                    break;
                byte[] buf = String.format("%s: %s\n", name, line).getBytes();
                ((Client) client).send(buf, 0, buf.length);

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public void receive(Address sender, ByteBuffer buf) {
//        String msg = new String(buf.array(), buf.arrayOffset(), buf.limit());
//        System.out.println("-" + msg);
        try {
            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(buf.array());
            System.out.println(message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {
        String msg = new String(buf, offset, length);
        System.out.println(String.format("# %s\n", msg));
    }

    @Override
    public void connectionClosed(Connection conn, String reason) {
        client.stop();
        running = false;
        Util.close(in);
        System.out.println(String.format("Connection to %s closed: %s", conn.peerAddress(), reason));
    }

    @Override
    public void connectionEstablished(Connection conn) {
        System.out.println("Connection established");
    }

//    @Override
//    public void receive(Message msg) {
//        try {
//            Address address = msg.getSrc();
//
//            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(msg.getBuffer());
//
//            String channelName = channel.getClusterName();
//            String userName = channel.getName(address);
//            String text = message.getMessage();
//
////            System.out.println("["+channelName +"] " + " : " + userName + " : " + text);
////            gui.putMessage(channelName, nick, text);
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//    }
}
