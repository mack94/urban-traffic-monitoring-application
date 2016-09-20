package pl.edu.agh.pp.detector.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Maciej on 05.09.2016.
 * 21:17
 * Project: detector.
 */
public class ManagementReceiverAdapter extends ReceiverAdapter {

    private final Map<String, List<String>> channelUsers;
    private final JChannel managementChannel;

    public ManagementReceiverAdapter(Map<String, List<String>> channelUsers, JChannel managementChannel) {
        this.channelUsers = channelUsers;
        this.managementChannel = managementChannel;
    }

    @Override
    public void receive(Message msg) {
        synchronized (channelUsers) {
            AnomalyOperationProtos.AnomalyAction action;

            try {
                action = AnomalyOperationProtos.AnomalyAction.parseFrom(msg.getBuffer());

                AnomalyOperationProtos.AnomalyAction.ActionType actionType = action.getAction();
                String channelName = action.getChannel();
                String userName = action.getNickname();

                switch (actionType) {
                    case JOIN:
                        if (!channelUsers.containsKey(channelName))
                            channelUsers.put(channelName, new LinkedList<String>());
                        channelUsers.get(channelName).add(userName);
                        System.out.println(userName + " joined to " + channelName);
                        break;

                    case LEAVE:
                        channelUsers.get(channelName).remove(userName);
                        if (channelUsers.get(channelName).isEmpty())
                            channelUsers.remove(channelName);
                        break;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (channelUsers) {
            AnomalyOperationProtos.AnomalyState.Builder builder = AnomalyOperationProtos.AnomalyState.newBuilder();

            for (Map.Entry<String, List<String>> entry : channelUsers.entrySet()) {
                String channelName = entry.getKey();
                List<String> users = entry.getValue();

                for (String user : users) {
                    builder.addStateBuilder()
                            .setAction(AnomalyOperationProtos.AnomalyAction.ActionType.JOIN)
                            .setChannel(channelName)
                            .setNickname(user);
                }
            }

            AnomalyOperationProtos.AnomalyState state = builder.build();

            state.writeTo(output);
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized (channelUsers) {
            AnomalyOperationProtos.AnomalyState state = AnomalyOperationProtos.AnomalyState.parseFrom(input);
            channelUsers.clear();

            for (AnomalyOperationProtos.AnomalyAction AnomalyAction : state.getStateList()) {
                String channelName = AnomalyAction.getChannel();
                String nick = AnomalyAction.getNickname();

                if (!channelUsers.containsKey(channelName)) {
                    channelUsers.put(channelName, new LinkedList<String>());
                }

                channelUsers.get(channelName).add(nick);
            }
        }
    }
}
