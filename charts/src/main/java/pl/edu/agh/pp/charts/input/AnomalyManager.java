package pl.edu.agh.pp.charts.input;

import ch.qos.logback.classic.Logger;
import javafx.scene.chart.XYChart;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dawid on 2016-10-23.
 */
public class AnomalyManager {
    private List<Anomaly> anomalyList;
    private static AnomalyManager instance;
    private MainWindowController mainWindowController;
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);

    private AnomalyManager(){
        anomalyList = new ArrayList<>();
    }

    public static AnomalyManager getInstance(){
        if(instance == null){
            instance = new AnomalyManager();
        }
        return instance;

    }

    public void setController(MainWindowController mainWindowController){
        this.mainWindowController = mainWindowController;
    }

    public void addAnomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        String id = String.valueOf(anomalyMessage.getAnomalyID());
        Anomaly anomaly;
        if(anomalyExists(id)){
            anomaly = getAnomalyById(id);
            anomaly.addMessage(anomalyMessage);
            buildChart(anomaly);
            if(mainWindowController != null) mainWindowController.updateAnomalyInfo(anomaly.getScreenMessage());
        }
        else{
            anomaly = new Anomaly(anomalyMessage);
            anomalyList.add(anomaly);
            if(mainWindowController != null) mainWindowController.addAnomalyToList(anomaly.getScreenMessage());
        }
    }

    private boolean anomalyExists(String anomalyId){
        for(Anomaly a: anomalyList){
            if(a.getAnomalyId().equals(anomalyId)) return true;
        }
        return false;
    }

    public Anomaly getAnomalyById(String id){
        for(Anomaly a: anomalyList){
            if(a.getAnomalyId().equals(id)) return a;
        }
        return null;
    }

    public Anomaly getAnomalyByScreenId(String screenId){
        for(Anomaly a: anomalyList){
            if(a.getScreenMessage().equals(screenId)) return a;
        }
        return null;
    }

    public void removeAnomaly(String anomalyId){
        Anomaly anomaly = getAnomalyById(anomalyId);
        if(anomaly != null && anomalyList.contains(anomaly)) {
            anomalyList.remove(anomaly);
        }
        else{
            logger.error("Trying to remove anomaly that doesn't exist");
        }
        mainWindowController.removeAnomalyFromList(anomaly.getScreenMessage());
    }

    public void buildChart(String anomalyId){
        buildChart(getAnomalyById(anomalyId));
    }

    private void buildChart(Anomaly anomaly){
        try {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            Map<String, String> durationHistory = anomaly.getDurationHistory();
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat hours = new SimpleDateFormat("HH");
            SimpleDateFormat minutes = new SimpleDateFormat("mm");
            for (String time : durationHistory.keySet()) {
                double h = Integer.valueOf(hours.format(parser.parse(time)));
                double m = Integer.valueOf(minutes.format(parser.parse(time)));
                System.out.println("adding: " + (h + (m / 60)) + " with dur: "+ durationHistory.get(time));
                XYChart.Data<Number,Number> data = new XYChart.Data<>(h + (m / 60), Integer.valueOf(durationHistory.get(time)));

                series.getData().add(data);
            }
            anomaly.setChartSeries(series);
        }
        catch (ParseException e){
            logger.error("chartException parse exception");
        }
    }

    public XYChart.Series<Number, Number> getChartData(String anomalyId){
        Anomaly anomaly = getAnomalyById(anomalyId);
        XYChart.Series<Number, Number> series = anomaly.getChartSeries();
        if(series == null) buildChart(anomaly);
        return anomaly.getChartSeries();
    }

    public XYChart.Series<Number, Number> getChartData(Anomaly anomaly){
        XYChart.Series<Number, Number> series = anomaly.getChartSeries();
        if(series == null) buildChart(anomaly);
        return anomaly.getChartSeries();
    }
}
