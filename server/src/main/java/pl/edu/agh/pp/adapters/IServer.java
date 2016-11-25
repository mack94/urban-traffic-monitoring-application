package pl.edu.agh.pp.adapters;

import org.jgroups.blocks.cs.Receiver;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Maciej on 25.11.2016.
 * 22:46
 * Project: server.
 */
public interface IServer extends Receiver {

    /**
     * Starts the desired server. It creates suitable socket, bind to the address forwarded by arguments.
     * Connection starts on the desired port in one of two possible modes. The first one is the NIO communication
     * (non-blocking i/o) and the second one is TCP communication. Both can be used, but implicitly the TCP
     * communication is used.
     *
     * @param bind_addr  The address which bind to this server.
     * @param port       The port which is default for this server.
     * @param nio        Communication mode which is used by this server: true if NIO, false otherwise (TCP).
     * @param serverName The name of the server, which is unique and is used only to register the server in the registry.
     * @throws Exception
     * @see org.jgroups.blocks.cs.NioServer
     * @see org.jgroups.blocks.cs.TcpServer
     * @see org.jgroups.blocks.cs.NioClient
     * @see org.jgroups.blocks.cs.TcpClient
     * @see org.jgroups.blocks.cs.BaseServer
     * @see org.jgroups.jmx.JmxConfigurator
     */
    void start(InetAddress bind_addr, int port, boolean nio, String serverName) throws Exception;


    /**
     * The method that is used to send the message to all receivers of this server.
     * The message must be delivered to the nodes. It'a a reliable group communication.
     *
     * @param buf The message to send in ByteBuffer form.
     */
    void send(ByteBuffer buf);


    /**
     * The method that is used to send the message to all receivers of this server.
     * The message must be delivered to the nodes. It'a a reliable group communication.
     *
     * @param buf The message to send in array of bytes form.
     */
    void send(byte[] buf);

}
