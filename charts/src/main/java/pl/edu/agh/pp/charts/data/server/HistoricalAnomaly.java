package pl.edu.agh.pp.charts.data.server;

import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-20.
 */
public class HistoricalAnomaly {
    Integer routeID;
    DateTime date;
    String anomalyID = null;
    Map<Integer, Integer> duration = null;
    List<HistoricalAnomaly> anomalies = null;
    HistoricalAnomaly(Integer routeID, DateTime date, Map<String,Map<Integer,Integer>> anomalies) {
        this.routeID = routeID;
        this.date = date;
        this.anomalies = new ArrayList<>();
        for(String anomalyID: anomalies.keySet()){
            this.anomalies.add(new HistoricalAnomaly(routeID,date,anomalyID,anomalies.get(anomalyID)));
        }
    }
    private HistoricalAnomaly(Integer routeID, DateTime date, String anomalyID,Map<Integer,Integer> duration) {
        this.routeID = routeID;
        this.date = date;
        this.anomalyID = anomalyID;
        this.duration = duration;
    }

    public Integer getRouteID() {
        return routeID;
    }

    public DateTime getDate() {
        return date;
    }

    public List<HistoricalAnomaly> getAnomalies() {
        return anomalies;
    }

    public XYChart.Series<Number, Number> getHistoricalAnomalySeries() {
        return buildHistoricalAnomalySeries();
    }

    private XYChart.Series<Number, Number> buildHistoricalAnomalySeries() {
        XYChart.Series<Number, Number> historicalAnomalySeries = new XYChart.Series<>();
        for (Integer time : duration.keySet()) {
            double h = time / 3600;
            double m = (time / 60) - (h * 60);
            XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), duration.get(time));

            historicalAnomalySeries.getData().add(data);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        historicalAnomalySeries.setName("Anomaly: " +anomalyID);
        return historicalAnomalySeries;
    }
}
