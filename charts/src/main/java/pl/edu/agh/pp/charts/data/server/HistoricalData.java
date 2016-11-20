package pl.edu.agh.pp.charts.data.server;

import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-12.
 */
public class HistoricalData {
    Integer routeID;
    DateTime date;
    Map<Integer, Integer> duration;

    public HistoricalData(Integer routeID, DateTime date, Map<Integer, Integer> duration) {
        this.routeID = routeID;
        this.date = date;
        this.duration = duration;
    }

    public Map<Integer, Integer> getDuration() {
        return duration;
    }

    public Integer getRouteID() {
        return routeID;
    }

    public DateTime getDate() {
        return date;
    }

    public XYChart.Series<Number, Number> getHistoricalDataSeries() {
        return buildHistoricalDataSeries();
    }

    private XYChart.Series<Number, Number> buildHistoricalDataSeries() {
        XYChart.Series<Number, Number> historicalDataSeries = new XYChart.Series<>();
        for (Integer time : duration.keySet()) {
            double h = time / 3600;
            double m = (time / 60) - (h * 60);
                XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), duration.get(time));
                historicalDataSeries.getData().add(data);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        historicalDataSeries.setName(routeID + ". " +sdf.format(date.toDate()));
        return historicalDataSeries;
    }
}
