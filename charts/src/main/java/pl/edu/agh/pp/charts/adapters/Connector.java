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
            String message = anomalyMessage.getMessage();
            DateTime dateTime = DateTime.now();
            int duration = anomalyMessage.getDuration();
            Color color = Color.CRIMSON;
            controller.putAnomalyMessageonScreen(id, message, dateTime, duration, color);
        }
    }

    public static void onWajcha(boolean wajchaFlag) {
        if (wajchaFlag) {
            logger.info("Connector :: MAKOWKA BO WAJCHA ZOSTALA WLACZONA! / FOR TRYLU: https://scontent-cdg2-1.xx.fbcdn.net/v/t1.0-9/14390956_1766216230317077_2567133355169022078_n.jpg?oh=696cafd5ecb1f18704ab4e57b62c3b38&oe=5879166A");
        } else {
            logger.info("Connector :: MAKOWKA BO WAJCHA ZOSTALA WYLACZONA! / FOR TRYLU: https://scontent-cdg2-1.xx.fbcdn.net/v/t1.0-9/14344142_1765711437034223_822234688098249504_n.jpg?oh=c52c4b021982c93af59b00ee80fffccc&oe=5839ED42");
        }
        logger.info("David, decide which is better -> priv.");
    }
}
