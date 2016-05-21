package pl.edu.agh.pp.charts.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.input.Input;
import pl.edu.agh.pp.charts.parser.Parser;

import java.io.File;
import java.util.Map;

/**
 * Created by Dawid on 2016-05-20.
 */
public class MainWindowController {
    private Stage primaryStage = null;
    private FileChooser fileChooser = null;
    private Parser parser;
    private Input input;
    private ObservableList<String> daysList = FXCollections.observableArrayList();
    private ObservableList<String> idsList = FXCollections.observableArrayList();
    private ObservableList<String> typesList = FXCollections.observableArrayList();

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
        startButton.setDefaultButton(true);
        clearCheckBox.setSelected(true);
        typeComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = typeComboBox.getSelectionModel().getSelectedItem();
                if(value.equals("Exact date")) {
                    fillInDates();
                } else if(value.equals("Aggregated day of week")) {
                    fillInDaysOfWeek();
                }
            }
        });
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
    private CheckBox durationCheckBox;
    @FXML
    private ComboBox<String> idComboBox;
    @FXML
    private ComboBox<String> dayComboBox;
    @FXML
    private CheckBox clearCheckBox;
    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private void handleFileButtonAction(ActionEvent e){
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file==null){
            return;
        }
        parser = new Parser(file);
        input = new Input();
        parser.parse(input);

        for(String id : input.getIds()){
            idsList.add(id);
        }
        idComboBox.setItems(idsList);

        fillInDates();

        typesList.add("Exact date");
        typesList.add("Aggregated day of week");
        typeComboBox.setItems(typesList);
    }

    private void fillInDates() {
        clearDayComboBox();
        for(String day : input.getDays()) {
            daysList.add(day);
        }
        dayComboBox.setItems(daysList);
        int size = daysList.size();
        dayComboBox.setVisibleRowCount(size < 8 ? size : 7);
    }

    private void fillInDaysOfWeek() {
        clearDayComboBox();
        daysList.add("Monday");
        daysList.add("Tuesday");
        daysList.add("Wednesday");
        daysList.add("Thursday");
        daysList.add("Friday");
        daysList.add("Saturday");
        daysList.add("Sunday");
        dayComboBox.setItems(daysList);
        dayComboBox.setVisibleRowCount(7);
    }

    private void clearDayComboBox() {
        daysList = FXCollections.observableArrayList();
        dayComboBox.setItems(daysList);
    }

    @FXML
    private void handleStartAction(ActionEvent e){
        if(dayComboBox.getSelectionModel().getSelectedItem() == null
                || idComboBox.getSelectionModel().getSelectedItem() == null
                || typeComboBox.getSelectionModel().getSelectedItem() == null){
            warn.setText("Select all parameters");
            return;
        }
        warn.setText("");
        if(clearCheckBox.isSelected()) {
            lineChart.getData().clear();
        }

        XYChart.Series<Number, Number> seriesDurationInTraffic = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDuration = new XYChart.Series<>();

        String type = typeComboBox.getSelectionModel().getSelectedItem();
        String day = dayComboBox.getSelectionModel().getSelectedItem();
        String id = idComboBox.getSelectionModel().getSelectedItem();
        Map<Double, Double> trafficValues = null;
        Map<Double, Double> normalValues = null;
        if(type.equals("Exact date")) {
            trafficValues = input.getData(day, id, true, false);
            if(durationCheckBox.isSelected()) normalValues = input.getData(day, id, false, false);
        } else if(type.equals("Aggregated day of week")) {
            day = day.substring(0, 3).toUpperCase();
            trafficValues = input.getData(day, id, true, true);
            if(durationCheckBox.isSelected()) normalValues = input.getData(day, id, false, true);
        }

        for(Double key : trafficValues.keySet()) {
            seriesDurationInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + idComboBox.getSelectionModel().getSelectedItem());
            seriesDurationInTraffic.getData().add(new XYChart.Data<Number, Number>(key, trafficValues.get(key)));
        }
        if(durationCheckBox.isSelected()) {
            for(Double key : normalValues.keySet()) {
                seriesDuration.setName("Duration - Day: " + day + ", ID: " + idComboBox.getSelectionModel().getSelectedItem());
                seriesDuration.getData().add(new XYChart.Data<Number, Number>(key, normalValues.get(key)));
            }
        }

        lineChart.getData().add(seriesDurationInTraffic);
        lineChart.getData().add(seriesDuration);

    }
    @FXML
    private void handleDurationAction(ActionEvent e){
        handleStartAction(e);
    }
    @FXML
    private void handleClearOnDrawAction(ActionEvent e){
        if(clearCheckBox.isSelected()) {
            lineChart.getData().clear();
            handleStartAction(e);
        }
    }
    @FXML
    private void handleClearAction(ActionEvent e){
        lineChart.getData().clear();
    }
}
