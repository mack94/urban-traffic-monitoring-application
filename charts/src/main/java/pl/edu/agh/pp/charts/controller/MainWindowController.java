package pl.edu.agh.pp.charts.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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

    @FXML
    private void initialize(){
        anomaliesTextArea.setEditable(false);
    }
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Button chartsButton;
    @FXML
    private TextArea anomaliesTextArea;

    @FXML
    private void handleChartsButtonAction(ActionEvent e) {
        if(chartsController == null){
            chartsController = new ChartsController(primaryStage,this);
        }
        chartsController.show();
    }
}
