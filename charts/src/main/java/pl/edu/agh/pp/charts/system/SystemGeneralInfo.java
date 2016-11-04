package pl.edu.agh.pp.charts.system;

import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Maciej on 04.11.2016.
 * 17:44
 * Project: charts.
 */
public class SystemGeneralInfo {

    private static int anomalyLiveTime;
    private static int baselineWindowSize;
    private static double leverValue;
    private static int port;
    private static String routes;
    private static AnomalyOperationProtos.SystemGeneralMessage.Shift shift;
    private static String systemDate;
    //TODO: Check whether it's everything

    public static void setSystemGeneralMessage(AnomalyOperationProtos.SystemGeneralMessage systemGeneralMessage) {
        setAnomalyLiveTime(systemGeneralMessage.getAnomalyLiveTime());
        setBaselineWindowSize(systemGeneralMessage.getBaselineWindowSize());
        setLeverValue(systemGeneralMessage.getLeverValue());
        setPort(systemGeneralMessage.getPort());
        setRoutes(systemGeneralMessage.getRoutes());
        setShift(systemGeneralMessage.getShift());
        setSystemDate(systemGeneralMessage.getSystemDate());
        informViewAboutChanges();
    }

    public static void setAnomalyLiveTime(int anomalyLiveTime) {
        SystemGeneralInfo.anomalyLiveTime = anomalyLiveTime;
        informViewAboutChanges();
    }

    public static void setBaselineWindowSize(int baselineWindowSize) {
        SystemGeneralInfo.baselineWindowSize = baselineWindowSize;
        informViewAboutChanges();
    }

    public static void setLeverValue(double leverValue) {
        SystemGeneralInfo.leverValue = leverValue;
        informViewAboutChanges();
    }

    public static void setPort(int port) {
        SystemGeneralInfo.port = port;
        informViewAboutChanges();
    }

    public static void setRoutes(String routes) {
        SystemGeneralInfo.routes = routes;
        informViewAboutChanges();
    }

    public static void setShift(AnomalyOperationProtos.SystemGeneralMessage.Shift shift) {
        SystemGeneralInfo.shift = shift;
        informViewAboutChanges();
    }

    public static void setSystemDate(String systemDate) {
        SystemGeneralInfo.systemDate = systemDate;
        informViewAboutChanges();
    }

    public static int getAnomalyLiveTime() {
        return anomalyLiveTime;
    }

    public static int getBaselineWindowSize() {
        return baselineWindowSize;
    }

    public static double getLeverValue() {
        return leverValue;
    }

    public static int getPort() {
        return port;
    }

    public static String getRoutes() {
        return routes;
    }

    public static AnomalyOperationProtos.SystemGeneralMessage.Shift getShift() {
        return shift;
    }

    public static String getSystemDate() {
        return systemDate;
    }

    private static void informViewAboutChanges() {
        Connector.updateServerInfo(leverValue, anomalyLiveTime, baselineWindowSize, shift, port);
    }

}
