package pl.edu.agh.pp.charts.adapters;

import javafx.scene.paint.Color;
import org.joda.time.DateTime;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-09-12.
 */
public class Connector {
    private static MainWindowController controller = null;

    public static void setController(MainWindowController mainWindowController){
        controller = mainWindowController;
    }

    public static void onMessege(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        if(controller!=null){
            long id = anomalyMessage.getRouteIdx();
            String message = anomalyMessage.getMessage();
            DateTime dateTime = DateTime.now();
            Color color = Color.BLACK;
            controller.putAnomalyMessageonScreen(id,message,dateTime,color);
        }
    }
    public static void onWajcha(boolean wajchaFlag){
        if(wajchaFlag){
            System.out.println("MAKOWKA BO WAJCHA ZOSTALA WLACZONA!");
        }
        else {
            System.out.println("MAKOWKA BO WAJCHA ZOSTALA WYLACZONA!");
        }
    }
}
