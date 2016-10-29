package pl.edu.agh.pp.detector.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.blocks.cs.Receiver;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.ByteArrayDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Maciej on 30.10.2016.
 * 00:15
 * Project: server.
 */
public class ManagementServer extends ReceiverAdapter implements Receiver {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ManagementServer.class);

    @Override
    public void receive(Address sender, byte[] buf, int offset, int length) {

        int bytesRead = 0;
        byte[] result = buf.clone();

        logger.info("Message received");

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
            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(result_parsable);
            logger.info("\t Message parsing completed - success");
        } catch (InvalidProtocolBufferException e) {
            logger.error("ChannelReceiver: InvalidProtocolBufferException while parsing the received message. Error: " + e);
            logger.error("Following bytes received:");
            logger.error("\t\t" + Arrays.toString(buf));
        }

    }

    @Override
    public void receive(Address sender, ByteBuffer buf) {

    }
}
