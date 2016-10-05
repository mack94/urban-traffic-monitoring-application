package pl.edu.agh.pp.detector.service;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.cs.BaseServer;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import pl.edu.agh.pp.detector.adapters.ManagementReceiverAdapter;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maciej on 05.09.2016.
 * 11:35
 * Project: detector.
 */
public class CommunicationService implements ICommunicationService {

    private static final String MANAGEMENT_CHANNEL_NAME = "Traffic_Anomaly_Detector_Management0";
    protected BaseServer server;
    private String userName;
    private JChannel managementChannel;
    private Map<String, List<String>> channelUsers;
    private Map<String, JChannel> activeChannels;

    public CommunicationService() throws Exception {
        System.out.println("Chat Service started");
        channelUsers = new HashMap<>();
        activeChannels = new HashMap<>();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getAnomalyMessage() {
        return null;
    }

    @Override
    public void joinChannel(String channelName) throws Exception {
//        if (activeChannels.containsKey(channelName)) {
//            activeChannels.get(channelName);
//            System.out.println("Joined to channel: " + channelName + " successfully.");
//            return;
//        }
////        InetAddress address = InetAddress.getByName("192.168.1." + channelName);
//
//        InetAddress address = InetAddress.getByName(channelName);
//
////        if (!address.isMulticastAddress()) {
////            throw new Exception(address + "is not a multicast valid address!");
////        }
//
//        JChannel channel = new JChannel(false);
//        channel.setName(userName);
//
//        ProtocolStack protocolStack = new ProtocolStack();
//        channel.setProtocolStack(protocolStack);
//
//        setupProtocolStack(protocolStack, address);
//
//        channel.setReceiver(new ChannelReceiver(channel));
//        channel.connect(channelName);
//
//        activeChannels.put(channelName, channel);
//
//        AnomalyOperationProtos.AnomalyAction anomalyAction = AnomalyOperationProtos.AnomalyAction.newBuilder()
//                .setAction(AnomalyOperationProtos.AnomalyAction.ActionType.JOIN)
//                .setNickname(userName)
//                .setChannel(channelName)
//                .build();
//
//        Message msg = new Message(null, null, anomalyAction.toByteArray());
//
//        if (!channelUsers.containsKey(channelName)) {
//            channelUsers.put(channelName, new LinkedList<>());
//        }
//        managementChannel.send(msg);
    }

    @Override
    public void leaveChannel(String channelName) throws Exception {
        if (!activeChannels.containsKey(channelName))
            return;

        JChannel channel = activeChannels.get(channelName);
        activeChannels.remove(channelName);

        AnomalyOperationProtos.AnomalyAction anomalyAction = AnomalyOperationProtos.AnomalyAction.newBuilder()
                .setAction(AnomalyOperationProtos.AnomalyAction.ActionType.LEAVE)
                .setNickname(userName)
                .setChannel(channelName)
                .build();

        managementChannel.send(new Message(null, null, anomalyAction.toByteArray()));

        channel.close();
    }

    @Override
    public void joinManagementChannel() throws Exception {
        managementChannel = new JChannel(false);
        managementChannel.setName(userName);

        ProtocolStack protocolStack = new ProtocolStack();
        managementChannel.setProtocolStack(protocolStack);
        setupProtocolStack(protocolStack);

        managementChannel.setReceiver(new ManagementReceiverAdapter(channelUsers, managementChannel));

        managementChannel.connect(MANAGEMENT_CHANNEL_NAME);
        managementChannel.getState(null, 10000);
    }

    @Override
    public void leaveManagementChannel() {
        managementChannel.close();
    }

    @Override
    public void sendMessage(String channelName, String message) throws Exception {
        if (!activeChannels.containsKey(channelName))
            return;

        JChannel channel = activeChannels.get(channelName);

        AnomalyOperationProtos.AnomalyMessage msg = AnomalyOperationProtos.AnomalyMessage.newBuilder()
                .setMessage(message)
                .build();
        System.out.println("Still not sent");
        channel.send(new Message(null, null, msg.toByteArray()));
        System.out.println("Sent");
    }

    private void setupProtocolStack(ProtocolStack protocolStack) throws Exception {
        setupProtocolStack(protocolStack, null);
    }

    private void setupProtocolStack(ProtocolStack protocolStack, InetAddress address) throws Exception {

        Protocol tcp = new TCP();

//        if (address != null) {
//            tcp.setValue("bind_addr", address);
//        }
//        tcp.setValue("bind_port", 6789);
//        tcp.setValue("bind_addr", address);

        protocolStack
                .addProtocol(tcp)
                .addProtocol(new TCPPING())
                .addProtocol(new MPING())
                .addProtocol(new MERGE2())
                .addProtocol(new FD_SOCK())
                .addProtocol(
                        new FD_ALL()
                                .setValue("timeout", 12000)
                                .setValue("interval", 3000)
                )
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2().setValue("use_mcast_xmit", false))
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH())
//                .addProtocol(new RELAY()
//                    .setValue("site","det"))
                .init();
    }
}
