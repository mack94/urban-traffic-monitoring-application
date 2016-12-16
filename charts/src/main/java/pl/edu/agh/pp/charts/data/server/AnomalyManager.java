package pl.edu.agh.pp.charts.data.server;

import ch.qos.logback.classic.Logger;
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
            if (mainWindowController != null) mainWindowController.updateAnomalyInfo(anomaly.getAnomalyId());
        } else {
            anomaly = new Anomaly(anomalyMessage);
            Connector.demandBaseline(DayOfWeek.of(Integer.parseInt(anomaly.getDayOfWeek())), Integer.parseInt(anomaly.getRouteId()), "");
            anomalyList.add(anomaly);
            if (mainWindowController != null) {
                mainWindowController.addAnomalyToList(anomaly);
                mainWindowController.redrawAllAnomaliesChart();
            }
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
            if (a.getAnomalyId().toUpperCase().contains(id.toUpperCase())) return a;
        }
        return null;
    }

    public void removeFromList(String id) {
        for (Anomaly a : anomalyList) {
            if (a.getAnomalyId().contains(id)) {
                anomalyList.remove(a);
                return;
            }
        }
    }

    public void removeAnomaly(String anomalyId) {
        Anomaly anomaly = getAnomalyById(anomalyId);
        if (anomaly != null) {
            mainWindowController.removeAnomalyFromList(anomaly.getAnomalyId());
            removeFromList(anomalyId);
            logger.debug("Anomaly list size after remove: " + anomalyList.size());
        } else {
            logger.error("Anomaly manager: Trying to remove anomaly that doesn't exist");
        }
    }

    public void buildChart(String anomalyId) {
        buildChart(getAnomalyById(anomalyId));
    }

    private XYChart.Series<Number, Number> buildChart(Anomaly anomaly) {
        try {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            Map<String, String> durationHistory = anomaly.getDurationHistory();

            for (String time : durationHistory.keySet()) {
                double h = Integer.valueOf(hours.format(parser.parse(time)));
                double m = Integer.valueOf(minutes.format(parser.parse(time)));
                XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), Integer.valueOf(durationHistory.get(time)));
                series.getData().add(data);
            }
            series.setName(anomaly.getRouteId() + ". " + anomaly.getRoute());
            return series;
        } catch (ParseException e) {
            logger.error("chartException parse exception");
        }
        return null;
    }

    private XYChart.Series<Number, Number> buildPercentChart(Anomaly anomaly) {
        try {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            Map<String, String> percentHistory = anomaly.getPercentHistory();

            for (String time : percentHistory.keySet()) {
                double h = Integer.valueOf(hours.format(parser.parse(time)));
                double m = Integer.valueOf(minutes.format(parser.parse(time)));
                XYChart.Data<Number, Number> data = new XYChart.Data<>(h + (m / 60), Integer.valueOf(percentHistory.get(time)));
                series.getData().add(data);
            }
            series.setName(anomaly.getRouteId() + ". " + anomaly.getRoute());
            return series;
        } catch (ParseException e) {
            logger.error("chartException parse exception");
        }
        return null;
    }

    public XYChart.Series<Number, Number> getChartData(String anomalyId) {
        Anomaly anomaly = getAnomalyById(anomalyId);
        return buildChart(anomaly);
    }

    public XYChart.Series<Number, Number> getChartData(Anomaly anomaly) {
        return buildChart(anomaly);
    }

    public XYChart.Series<Number, Number> getBaseline(Anomaly anomaly) {
        return anomaly.getBaselineSeries();
    }

    public XYChart.Series<Number, Number> getPercentChartData(String id) {
        return buildPercentChart(getAnomalyById(id));
    }

    public void clearAnomalies() {
        int size = anomalyList.size();
        for (int i = 0; i < size; i++) {
            Anomaly anomaly = anomalyList.get(0);
            if (anomaly != null) {
                mainWindowController.removeAnomalyFromList(anomaly.getAnomalyId());
                removeFromList(anomaly.getAnomalyId());
                logger.debug("Anomaly list size after remove: " + anomalyList.size());
            } else {
                logger.error("Anomaly manager: Trying to remove anomaly that doesn't exist");
            }
        }
    }
}
