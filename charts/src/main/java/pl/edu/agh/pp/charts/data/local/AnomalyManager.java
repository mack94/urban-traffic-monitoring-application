package pl.edu.agh.pp.charts.data.local;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dawid on 2016-10-23.
 */
public class AnomalyManager {
    private static AnomalyManager instance;
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);
    private List<Anomaly> anomalyList;
    private MainWindowController mainWindowController;
    private SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat hours = new SimpleDateFormat("HH");
    private SimpleDateFormat minutes = new SimpleDateFormat("mm");

    private AnomalyManager() {
        anomalyList = new ArrayList<>();
    }

    public static AnomalyManager getInstance() {
        if (instance == null) {
            instance = new AnomalyManager();
        }
        return instance;

    }

    public void setController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public void addAnomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        String id = String.valueOf(anomalyMessage.getAnomalyID());
        Anomaly anomaly;
        if (anomalyExists(id)) {
            anomaly = getAnomalyById(id);
            anomaly.addMessage(anomalyMessage);
            if(anomaly.getChartSeries() != null) {
                addPointToChart(anomaly, anomalyMessage.getDate());
            }
            if (mainWindowController != null) mainWindowController.updateAnomalyInfo(anomaly.getScreenMessage());
        } else {
            anomaly = new Anomaly(anomalyMessage);
            Connector.demandBaseline(DayOfWeek.of(Integer.parseInt(anomaly.getDayOfWeek())), Integer.parseInt(anomaly.getRouteId()));
            anomalyList.add(anomaly);
            if (mainWindowController != null) mainWindowController.addAnomalyToList(anomaly.getScreenMessage());
        }
    }

    private boolean anomalyExists(String anomalyId) {
        for (Anomaly a : anomalyList) {
            if (a.getAnomalyId().equals(anomalyId)) return true;
        }
        return false;
    }

    public Anomaly getAnomalyById(String id) {
        for (Anomaly a : anomalyList) {
            if (a.getAnomalyId().equals(id)) return a;
        }
        return null;
    }

    public Anomaly getAnomalyByScreenId(String screenId) {
        for (Anomaly a : anomalyList) {
            if (a.getScreenMessage().equals(screenId)) return a;
        }
        return null;
    }

    public void removeAnomaly(String anomalyId) {
        Anomaly anomaly = getAnomalyById(anomalyId);
        if (anomaly != null && anomalyList.contains(anomaly)) {
            anomalyList.remove(anomaly);
            mainWindowController.removeAnomalyFromList(anomaly.getScreenMessage());
        } else {
            logger.error("Trying to remove anomaly that doesn't exist");
        }
    }

    public void buildChart(String anomalyId) {
        buildChart(getAnomalyById(anomalyId));
    }

    private void addPointToChart(Anomaly anomaly, String time) {
        try {
            XYChart.Series<Number, Number> series = anomaly.getChartSeries();
            double h = Integer.valueOf(hours.format(parser.parse(time)));
            double m = Integer.valueOf(minutes.format(parser.parse(time)));
            XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), Integer.valueOf(anomaly.getDurationHistory().get(time)));
            Platform.runLater(() -> series.getData().add(data));
        } catch (ParseException e) {
            logger.error("chartException parse exception");
        }
    }

    private void buildChart(Anomaly anomaly) {
        try {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            Map<String, String> durationHistory = anomaly.getDurationHistory();

            for (String time : durationHistory.keySet()) {
                double h = Integer.valueOf(hours.format(parser.parse(time)));
                double m = Integer.valueOf(minutes.format(parser.parse(time)));
                XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), Integer.valueOf(durationHistory.get(time)));

                series.getData().add(data);
            }
            anomaly.setChartSeries(series);
        } catch (ParseException e) {
            logger.error("chartException parse exception");
        }
    }

    public XYChart.Series<Number, Number> getChartData(String anomalyId) {
        Anomaly anomaly = getAnomalyById(anomalyId);
        XYChart.Series<Number, Number> series = anomaly.getChartSeries();
        if (series == null) buildChart(anomaly);
        return anomaly.getChartSeries();
    }

    public XYChart.Series<Number, Number> getChartData(Anomaly anomaly) {
        XYChart.Series<Number, Number> series = anomaly.getChartSeries();
        if (series == null) buildChart(anomaly);
        return anomaly.getChartSeries();
    }

    public XYChart.Series<Number, Number> getBaseline(Anomaly anomaly) {
        return anomaly.getBaselineSeries();
    }
}
