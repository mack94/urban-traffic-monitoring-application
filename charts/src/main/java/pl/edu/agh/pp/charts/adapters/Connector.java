package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.joda.time.DateTime;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {
    private static MainWindowController mainWindowController = null;

    public static void setController(MainWindowController mainWindowController){
        mainWindowController = mainWindowController;
    }

    public static void onMessege(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        if(mainWindowController!=null){
            long id = anomalyMessage.getRouteIdx();
            String message = anomalyMessage.getMessage();
            DateTime dateTime = DateTime.now();
            Color color = Color.BLACK;
            mainWindowController.putAnomalyMessageonScreen(id,message,dateTime,color);
        }
    }
}
