package pl.edu.agh.pp.detector.adapters;

import org.jgroups.Address;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.cs.BaseServer;
import org.jgroups.blocks.cs.NioServer;
import org.jgroups.blocks.cs.Receiver;
import org.jgroups.blocks.cs.TcpServer;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Maciej on 11.09.2016.
 * 14:36
 * Project: server.
 */
public class Server extends ReceiverAdapter implements Receiver {

    protected BaseServer server;

    public void start(InetAddress bind_addr, int port, boolean nio) throws Exception {
        server = nio ? new NioServer(bind_addr, port) : new TcpServer(bind_addr, port);
        server.receiver(this);
        server.start();
        JmxConfigurator.register(server, Util.getMBeanServer(), "pub:name=pub-server");
        int local_port = server.localAddress() instanceof IpAddress ? ((IpAddress) server.localAddress()).getPort() : 0;
        System.out.printf("\nServer listening at %s:%s\n", bind_addr != null ? bind_addr : "0.0.0.0", local_port);
    }


    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {
        try {
            server.send(null, buf, offset, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Address sender, ByteBuffer buf) {
        try {
            server.send(null, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(ByteBuffer buf) {
        try {
            server.send(null, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AnomalyOperationProtos.AnomalyMessage msg = AnomalyOperationProtos.AnomalyMessage.parseFrom(buf.array());
            System.out.println("Sent: " + msg);
            // TODO: Could be removed i think.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] buf) {
        try {
            server.send(null, buf, 0, buf.length);
            AnomalyOperationProtos.AnomalyMessage msg = AnomalyOperationProtos.AnomalyMessage.parseFrom(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
