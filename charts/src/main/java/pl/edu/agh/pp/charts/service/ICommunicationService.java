package pl.edu.agh.pp.charts.service;

/**
 * Created by Maciej on 06.09.2016.
 * 11:04
 * Project: charts.
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
