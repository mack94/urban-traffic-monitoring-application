package pl.edu.agh.pp.detector.adapters;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.cs.*;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Maciej on 05.09.2016.
 * 21:26
 * Project: detector.
 */
public class ChannelReceiver extends ReceiverAdapter {

    private JChannel channel; // final
    protected BaseServer server;

    public void start(InetAddress bind_addr, int port, boolean nio) throws Exception {
        server=nio? new NioServer(bind_addr, port) : new TcpServer(bind_addr, port);
        server.receiver(this);
        server.start();
        JmxConfigurator.register(server, Util.getMBeanServer(), "pub:name=pub-server");
        int local_port = server.localAddress() instanceof IpAddress ? ((IpAddress) server.localAddress()).getPort(): 0;
        System.out.printf("\nPubServer listening at %s:%s\n", bind_addr != null? bind_addr : "0.0.0.0", local_port);
    }

    public ChannelReceiver() {
    }

    public ChannelReceiver(JChannel channel) {
        this.channel = channel;
    }

    @Override
    public void receive(Address sender, ByteBuffer buf) {
        try {
            server.send(null, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {
        try {
            server.send(null, buf, offset, length);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
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
