package pl.edu.agh.pp.charts.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Maciej on 06.09.2016.
 * 11:03
 * Project: charts.
 */
public class ChannelReceiver extends ReceiverAdapter {

    private final JChannel channel;

    public ChannelReceiver(JChannel channel) {
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
        try {
            Address address = msg.getSrc();

            AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.parseFrom(msg.getBuffer());

            String channelName = channel.getClusterName();
            String userName = channel.getName(address);
            String text = message.getMessage();

            System.out.println("["+channelName +"] " + " : " + userName + " : " + text);
//            gui.putMessage(channelName, nick, text);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
