package pl.edu.agh.pp.adapters;

import org.jgroups.Address;
import org.jgroups.blocks.cs.BaseServer;
import org.jgroups.blocks.cs.NioServer;
import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.blocks.cs.TcpServer;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Maciej on 25.11.2016.
 * 23:02
 * Project: server.
 */
public abstract class Server extends ReceiverAdapter implements IServer {

    protected BaseServer server;

    @Override
    public void start(InetAddress bind_addr, int port, boolean nio, String serverName) throws Exception {
        setServer(nio ? new NioServer(bind_addr, port) : new TcpServer(bind_addr, port));
        getServer().receiver(this);
        getServer().start();
        JmxConfigurator.register(getServer(), Util.getMBeanServer(), "pub:name=server-".concat(serverName));
        int local_port = getServer().localAddress() instanceof IpAddress
                ? ((IpAddress) getServer().localAddress()).getPort()
                : 0;
        System.out.printf(serverName + "\n listening at %s:%s\n", bind_addr != null ? bind_addr : "0.0.0.0", local_port);
    }

    private BaseServer getServer() {
        return server;
    }

    private void setServer(BaseServer server) {
        this.server = server;
    }

    @Override
    public void send(ByteBuffer buf) {
        try {
            server.send(null, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] buf) {
        try {
            server.send(null, buf, 0, buf.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public abstract void receive(Address sender, byte[] buf, int offset, int length);

    @Override
    public abstract void receive(Address sender, ByteBuffer buf);
}
