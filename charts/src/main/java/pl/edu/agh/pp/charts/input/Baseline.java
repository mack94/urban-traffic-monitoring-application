package pl.edu.agh.pp.charts.input;

import javafx.scene.chart.XYChart;

import java.time.DayOfWeek;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-05.
 */
public class Baseline {
    private Map<Integer, Integer> baseline;
    private Integer routeID;
    private XYChart.Series<Number, Number> baselineSeries = null;
    private DayOfWeek day;

    public Baseline(Integer routeID, DayOfWeek day,Map<Integer, Integer> baseline){
        this.baseline = baseline;
        this.routeID = routeID;
        this.day = day;
    }

    public Map<Integer, Integer> getBaseline() {
        return baseline;
    }

    public XYChart.Series<Number, Number> getBaselineSeries(){
        if(baselineSeries == null){
            buildBaselineSeries();
            return baselineSeries;
        }
        else{
            return baselineSeries;
        }
    }

    public Integer getRouteID() {
        return routeID;
    }

    public DayOfWeek getDay() {
        return day;
    }

    private void buildBaselineSeries(){
        System.out.println("Building baseline!");
        baselineSeries = new XYChart.Series<>();

        for (Integer time : baseline.keySet()) {
            double h = time/3600;
            double m = (time/60) - (h*60);
            XYChart.Data<Number,Number> data = new XYChart.Data<>(h + (m / 60), baseline.get(time));

            baselineSeries.getData().add(data);
        }
    }
}