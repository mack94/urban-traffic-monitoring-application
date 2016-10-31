package pl.edu.agh.pp.charts.settings;

/**
 * Created by Dawid on 2016-10-31.
 */
public class ServerOptions {
    private String leverValue = "";
    private String anomalyLiveTime = "";
    private String BaselineWindowSize = "";
    private String shift = "";
    private String anomalyPortNr;

    public String getLeverValue() {
        return leverValue;
    }

    public void setLeverValue(String leverValue) {
        this.leverValue = leverValue;
    }

    public String getAnomalyLiveTime() {
        return anomalyLiveTime;
    }

    public void setAnomalyLiveTime(String anomalyLiveTime) {
        this.anomalyLiveTime = anomalyLiveTime;
    }

    public String getBaselineWindowSize() {
        return BaselineWindowSize;
    }

    public void setBaselineWindowSize(String baselineWindowSize) {
        BaselineWindowSize = baselineWindowSize;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAnomalyPortNr() {
        return anomalyPortNr;
    }

    public void setAnomalyPortNr(String anomalyPortNr) {
        this.anomalyPortNr = anomalyPortNr;
    }

}
