package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Connector.class);
    private static MainWindowController controller = null;

    public static void setController(MainWindowController mainWindowController) {
        controller = mainWindowController;
    }

    public static void onMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        if (controller != null) {
            int id = anomalyMessage.getRouteIdx();
            String message = anomalyMessage.getMessage() + " _ " + anomalyMessage.getAnomalyID() + " _ ";
            DateTime dateTime = DateTime.now();
            int duration = anomalyMessage.getDuration();
            Color color = Color.CRIMSON;
            controller.putAnomalyMessageonScreen(id, message, dateTime, duration, color);
        }
    }

    public static void onLeverChange(String value){
        logger.info("Chnging lever to: " + value);
    }

}
