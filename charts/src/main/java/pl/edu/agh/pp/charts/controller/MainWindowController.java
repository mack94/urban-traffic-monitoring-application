package pl.edu.agh.pp.charts.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.input.Input;

/**
 * Created by Dawid on 2016-09-05.
 */
public class MainWindowController {
    private Stage primaryStage = null;
    private Input input;
    private ChartsController chartsController = null;

    public MainWindowController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/MainWindow.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setTitle("Urban traffic monitoring - charts");
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public void putAnomalyMessageonScreen(long id, String message, DateTime dateTime, Color color){
        Text text1 = new Text(id + ": " +  dateTime.toString() + " " +  message + "\n");
        text1.setFill(color);
        text1.setFont(Font.font("Helvetica", FontPosture.REGULAR, 16));
        anomaliesTextFlow.getChildren().add(0, text1);
    }

    @FXML
    private void initialize(){
        anomaliesTextFlow.setTextAlignment(TextAlignment.CENTER);
        anomaliesTextFlow.setMaxHeight(150);
        putAnomalyMessageonScreen(1, "Trasa 1 z place1 do place5 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(2, "Trasa 1 z place2 do place5 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(3, "Trasa 1 z place2 do place4 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(4, "Trasa 1 z place5 do place13 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(5, "Trasa 1 z place2 do place1 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(6, "Trasa 1 z place1 do place22 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(7, "Trasa 1 z place5 do place12 severity 1", DateTime.now(), Color.BLACK);
        putAnomalyMessageonScreen(8, "Trasa 1 z place7 do place1 severity 2", DateTime.now(), Color.ORANGE);
        putAnomalyMessageonScreen(9, "Trasa 1 z place2 do place4 severity 3", DateTime.now(), Color.GREEN);
        putAnomalyMessageonScreen(10, "Trasa 1 z place2 do place5 severity 4", DateTime.now(), Color.BLUE);
        putAnomalyMessageonScreen(11, "Trasa 1 z place11 do place3 severity 5", DateTime.now(), Color.RED);
    }
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Button chartsButton;
    @FXML
    private TextFlow anomaliesTextFlow;

    @FXML
    private void handleChartsButtonAction(ActionEvent e) {
        if(chartsController == null){
            chartsController = new ChartsController(primaryStage,this);
        }
        chartsController.show();
    }
    @FXML
    private void handleTestButtonAction(ActionEvent e) {

        putAnomalyMessageonScreen(666,"Test anomaly", DateTime.now(), Color.PINK);
    }
}
