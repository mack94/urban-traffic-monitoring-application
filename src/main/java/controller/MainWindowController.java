package main.java.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.Main;
import main.java.input.Input;
import main.java.input.Record;
import main.java.parser.Parser;

import java.io.File;

/**
 * Created by Dawid on 2016-05-20.
 */
public class MainWindowController {
    private Stage primaryStage = null;
    private FileChooser fileChooser = null;
    private Parser parser;
    private Input input;

    public MainWindowController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        input = new Input();
    }

    public void show(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/MainWindow.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setTitle("Urban traffic monitoring - charts");
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void initialize(){
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log Files", "*.log"));
        warn.setStyle("-fx-text-fill: red");
        lineChart.setTitle("");

    }
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Button fileButton;
    @FXML
    private Button startButton;
    @FXML
    private Label warn;
    @FXML
    private TextField idTextField;
    @FXML
    private TextField dayTextField;

    @FXML
    private void handleFileButtonAction(ActionEvent e){
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file==null){
            return;
        }
            parser = new Parser(file);
            input = new Input();
            parser.parse(input);
    }
    @FXML
    private void handleStartAction(ActionEvent e){
        lineChart.getData().clear();

        XYChart.Series<Number,Number> series = new XYChart.Series<Number, Number>();
        for(Record r: input.getInput()){
            if(r.getDay().equals(dayTextField.getText())&&r.getId().equals(idTextField.getText())) {
                series.setName("Day: "+r.getDay() +"th, ID: "+ idTextField.getText());
                series.getData().add(new XYChart.Data<Number, Number>(r.getTimeForChart(), Integer.valueOf(r.getDurationInTraffic())));
            }
        }
        lineChart.getData().add(series);


    }
}
