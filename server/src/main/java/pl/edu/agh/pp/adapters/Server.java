package pl.edu.agh.pp.adapters;

import org.jgroups.Address;
import org.jgroups.blocks.cs.BaseServer;
import org.jgroups.blocks.cs.NioServer;
import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.blocks.cs.TcpServer;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Maciej on 25.11.2016.
 * 23:02
 * Project: server.
 */
public abstract class Server extends ReceiverAdapter implements IServer {

    protected final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
    protected BaseServer server;
    protected String serverName;

    @Override
    public void start(InetAddress bind_addr, int port, boolean nio, String serverName) throws Exception {
        this.serverName = serverName;
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
            logger.info("Server: Message has been sent to all clients connected to this server: " + serverName);
        } catch (Exception e) {
            logger.error("Server[" + serverName + "]: Exception error occurred while " +
                    "sending the message to all clients: " + e, e);
        }
    }

    @Override
    public void send(byte[] buf) {
        try {
            server.send(null, buf, 0, buf.length);
            logger.info("Server: Message has been sent to all clients connected to this server: " + serverName);
        } catch (Exception e) {
            logger.error("Server[" + serverName + "]: Exception error occurred while " +
                    "sending the message to all clients: " + e, e);
        }
    }

    @Override
    public abstract void receive(Address sender, byte[] buf, int offset, int length);

    @Override
    public abstract void receive(Address sender, ByteBuffer buf);

}
