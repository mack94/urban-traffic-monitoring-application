package pl.edu.agh.pp.detector.service;

/**
 * Created by Maciej on 05.09.2016.
 * 11:31
 * Project: detector.
 */
public interface ICommunicationService {

    String getUserName();

    void setUserName(String userName);

    String getAnomalyMessage();

    void joinChannel(String channelName) throws Exception;

    void leaveChannel(String channelName) throws Exception;

    void joinManagementChannel() throws Exception;

    void leaveManagementChannel();

    void sendMessage(String channelName, String message) throws  Exception;

}
