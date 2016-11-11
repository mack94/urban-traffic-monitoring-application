package pl.edu.agh.pp.charts.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.data.local.Input;
import pl.edu.agh.pp.charts.data.local.ResourcesHolder;
import pl.edu.agh.pp.charts.parser.Parser;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class ChartsController {
    private Stage primaryStage = null;
    private Scene scene = null;
    private FileChooser fileChooser = null;
    private Parser parser;
    private Input input;
    private Set<Integer> idsSet = new HashSet<>();
    private Set<String> datesSet = new HashSet<>();
    private ObservableList<String> localRouteIdList = FXCollections.observableArrayList();
    private ObservableList<String> serverRouteIdList = FXCollections.observableArrayList();
    private MainWindowController parent;
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
    private Button button134;
    @FXML
    private Button button578;
    @FXML
    private Button reverseRouteButton;
    @FXML
    private Label logsListLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label dayLabel;
    @FXML
    private ComboBox<String> baselineTypeComboBox;
    @FXML
    private Label baselineTypeLabel;
    @FXML
    private HBox dayHBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private CheckBox drawBaselineCheckbox;
    @FXML
    private Label drawBaselineLabel;
    @FXML
    private ComboBox<String> sourceComboBox;
    @FXML
    private Label typeLabel;
    @FXML
    private HBox baselineBox;

    public ChartsController(Stage primaryStage, MainWindowController parent) {
        this.primaryStage = primaryStage;
        this.parent = parent;
        Connector.setChartsController(this);
    }

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/Charts.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    if (input != null) {
                        input.cleanUp();
                    }
                }
            });
            primaryStage.setTitle("Urban traffic monitoring - charts");
            scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            baselineBox.managedProperty().bind(baselineTypeComboBox.visibleProperty());
            primaryStage.setScene(scene);
//            primaryStage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setScene(){
        primaryStage.setScene(scene);
    }
    @FXML
    private void initialize() {
        datePicker = new DatePicker();
        dayComboBox = new ComboBox<String>();
        dayLabel = new Label("Day");
        fileChooser = new FileChooser();
        parser = new Parser();
        input = new Input();
        File file = new File("./");
        fileChooser.setInitialDirectory(file);
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log Files", "*.log"));
        warn.setStyle("-fx-text-fill: red");
        lineChart.setTitle("");
        startButton.setDefaultButton(true);
        clearCheckBox.setSelected(true);
        typeComboBox.setVisible(false);
        typeLabel.setVisible(false);
        Image reverseButtonImage = new Image(Main.class.getResourceAsStream("/reverse.png"));
        reverseRouteButton.setGraphic(new ImageView(reverseButtonImage));
        dayComboBox.setVisible(false);
        idComboBox.setVisible(false);
        idLabel.setVisible(false);
        reverseRouteButton.setVisible(false);
        baselineTypeComboBox.setVisible(false);
        baselineTypeLabel.setVisible(false);
        baselineTypeComboBox.getItems().addAll("Normal","Holidays");
        fillInDaysOfWeek();
        drawBaselineCheckbox.setVisible(false);
        drawBaselineLabel.setVisible(false);
        sourceComboBox.getItems().addAll("Local Data","Server Data");
    }

    @FXML
    private void handleFileButtonAction(ActionEvent e) {
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        if (files == null || files.isEmpty()) {
            return;
        }
        input.getRoutes();
        for (File file : files) {
            ResourcesHolder.getInstance().addLog(file.getName());
            parser.setFile(file);
            parser.parse(input);
        }

        for (String id : input.getIds()) {
            idsSet.add(Integer.parseInt(id));
        }
        List<Integer> ids = new ArrayList<>(idsSet);
        Collections.sort(ids);
        for (Integer id : ids) {
            localRouteIdList.add(input.getRoute(String.valueOf(id)));
        }
        setupFields();
    }

    public void setServerRouteIds(){
        //TODO
    }

    public void setServerDates(){
        //TODO
    }

    private void setupFields() {
        localRouteIdList.clear();
        if(sourceComboBox.getSelectionModel().getSelectedItem()!=null && sourceComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("local data")){
            idComboBox.setItems(localRouteIdList);
        }
        else {
            idComboBox.setItems(serverRouteIdList);
        }
        setupDatePicker();
    }

    private void fillInDaysOfWeek() {
        dayComboBox.getItems().clear();
        dayComboBox.getItems().addAll("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday");
        dayComboBox.setVisibleRowCount(7);
    }

    private void createTooltips() {
        for (XYChart.Series<Number, Number> s : lineChart.getData()) {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                double num = (double) d.getXValue();
                long iPart;
                double fPart;
                iPart = (long) num;
                fPart = num - iPart;
                Tooltip.install(d.getNode(), new Tooltip("Time of the day: " + iPart + "h " + (long) (fPart * 60) + "min" + "\nDuration: " + d.getYValue().toString() + " seconds"));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void setupDatePicker(){
        datePicker = new DatePicker();
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker tmoDatePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                if(!availableDate(date))
                                    setDisable(true);
                            }
                        };
                    }
                };
        datePicker.setDayCellFactory(dayCellFactory);
        if("historic data".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel, datePicker);
        }
    }

    private boolean availableDate(LocalDate date){
        if("local data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())){
            return input.getDays().contains((date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()));
        }
        else{
            //todo implement server historic data
            return false;
        }
    }

    @FXML
    private void handleStartAction(ActionEvent e) {
        if (idComboBox.getSelectionModel().getSelectedItem() == null
                || typeComboBox.getSelectionModel().getSelectedItem() == null) {
            if("historic data".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
                if(dayComboBox.getSelectionModel().getSelectedItem() == null) {
                    warn.setText("Select all parameters");
                    return;
                }
            }
            else{
                if(datePicker.getValue() == null){
                    warn.setText("Select all parameters");
                    return;
                }
            }
        }
        warn.setText("");
        if (clearCheckBox.isSelected()) {
            lineChart.getData().clear();
        }

        XYChart.Series<Number, Number> seriesDurationInTraffic = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDuration = new XYChart.Series<>();

        String type = typeComboBox.getSelectionModel().getSelectedItem();
        String day;
        if("baseline".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            day = dayComboBox.getSelectionModel().getSelectedItem();
        }
        else{
            day = datePicker.getValue().toString();
        }
        String id = input.getId(idComboBox.getSelectionModel().getSelectedItem());
        Map<Double, Double> trafficValues = null;
        Map<Double, Double> normalValues = null;
        if ("historic data".equalsIgnoreCase(type)) {
            trafficValues = input.getData(day, id, true, false);
            if (durationCheckBox.isSelected()) normalValues = input.getData(day, id, false, false);
        } else if (type.equals("Aggregated day of week")) {
            day = day.substring(0, 3).toUpperCase();
            trafficValues = input.getData(day, id, true, true);
            if (durationCheckBox.isSelected()) normalValues = input.getData(day, id, false, true);
        }

        for (Double key : trafficValues.keySet()) {
            seriesDurationInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + idComboBox.getSelectionModel().getSelectedItem());
            seriesDurationInTraffic.getData().add(new XYChart.Data<Number, Number>(key, trafficValues.get(key)));
        }
        if (durationCheckBox.isSelected()) {
            for (Double key : normalValues.keySet()) {
                seriesDuration.setName("Duration - Day: " + day + ", ID: " + idComboBox.getSelectionModel().getSelectedItem());
                seriesDuration.getData().add(new XYChart.Data<Number, Number>(key, normalValues.get(key)));
            }
        }

        lineChart.getData().add(seriesDurationInTraffic);
        lineChart.getData().add(seriesDuration);

        createTooltips();
    }

    @FXML
    private void handleSummaryAction1(ActionEvent e) {
        drawSummaryChart(4);
    }

    @FXML
    private void handleSummaryAction2(ActionEvent e) {
        drawSummaryChart(8);
    }

    private void drawSummaryChart(int route) {
        if (dayComboBox.getSelectionModel().getSelectedItem() == null
                || typeComboBox.getSelectionModel().getSelectedItem() == null) {
            warn.setText("You have to select type and date!");
            return;
        }
        warn.setText("");
        if (clearCheckBox.isSelected()) {
            lineChart.getData().clear();
        }

        XYChart.Series<Number, Number> seriesDurationInTraffic = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDuration = new XYChart.Series<>();

        XYChart.Series<Number, Number> seriesDurationSummaryInTraffic = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDurationSummary = new XYChart.Series<>();

        String type = typeComboBox.getSelectionModel().getSelectedItem();
        String day = dayComboBox.getSelectionModel().getSelectedItem();
        Map<Double, Double> summaryTraffic = null;
        Map<Double, Double> traffic = null;

        Map<Double, Double> durationSummaryTraffic = null;
        Map<Double, Double> durationTraffic = null;

        if (type.equals("Exact date")) {
            if (route == 4) {
                summaryTraffic = input.getSummary(day, 1, 3, true, false);
                traffic = input.getData(day, String.valueOf(route), true, false);
                if (durationCheckBox.isSelected()) {
                    durationSummaryTraffic = input.getSummary(day, 1, 3, false, false);
                    durationTraffic = input.getData(day, String.valueOf(route), false, false);
                }
            } else if (route == 8) {
                summaryTraffic = input.getSummary(day, 5, 7, true, false);
                traffic = input.getData(day, String.valueOf(route), true, false);
                if (durationCheckBox.isSelected()) {
                    durationSummaryTraffic = input.getSummary(day, 5, 7, false, false);
                    durationTraffic = input.getData(day, String.valueOf(route), false, false);
                }
            }
        } else if (type.equals("Aggregated day of week")) {
            day = day.substring(0, 3).toUpperCase();
            if (route == 4) {
                summaryTraffic = input.getSummary(day, 1, 3, true, true);
                traffic = input.getData(day, String.valueOf(route), true, true);
                if (durationCheckBox.isSelected()) {
                    durationSummaryTraffic = input.getSummary(day, 1, 3, false, true);
                    durationTraffic = input.getData(day, String.valueOf(route), false, true);
                }
            } else if (route == 8) {
                summaryTraffic = input.getSummary(day, 5, 7, true, true);
                traffic = input.getData(day, String.valueOf(route), true, true);
                if (durationCheckBox.isSelected()) {
                    durationSummaryTraffic = input.getSummary(day, 5, 7, false, true);
                    durationTraffic = input.getData(day, String.valueOf(route), false, true);
                }
            }
        }

        String ids = route == 4 ? "1-3" : "5-7";
        for (Double key : summaryTraffic.keySet()) {
            seriesDurationSummaryInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + ids);
            seriesDurationSummaryInTraffic.getData().add(new XYChart.Data<Number, Number>(key, summaryTraffic.get(key)));
        }
        if (durationCheckBox.isSelected()) {
            for (Double key : durationSummaryTraffic.keySet()) {
                seriesDurationSummary.setName("Duration - Day: " + day + ", ID: " + ids);
                seriesDurationSummary.getData().add(new XYChart.Data<Number, Number>(key, durationSummaryTraffic.get(key)));
            }
        }

        for (Double key : traffic.keySet()) {
            seriesDurationInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + route);
            seriesDurationInTraffic.getData().add(new XYChart.Data<Number, Number>(key, traffic.get(key)));
        }
        if (durationCheckBox.isSelected()) {
            for (Double key : durationTraffic.keySet()) {
                seriesDuration.setName("Duration - Day: " + day + ", ID: " + route);
                seriesDuration.getData().add(new XYChart.Data<Number, Number>(key, durationTraffic.get(key)));
            }
        }

        lineChart.getData().add(seriesDurationSummaryInTraffic);
        lineChart.getData().add(seriesDurationSummary);
        lineChart.getData().add(seriesDurationInTraffic);
        lineChart.getData().add(seriesDuration);

        createTooltips();
    }

    @FXML
    private void handleDurationAction(ActionEvent e) {
        handleStartAction(e);
    }

    @FXML
    private void handleTypeAction(ActionEvent e) {
        dayComboBox.setVisible(true);
        idComboBox.setVisible(true);
        idLabel.setVisible(true);
        dayLabel.setVisible(true);
        reverseRouteButton.setVisible(true);
        if("baseline".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())){
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel,dayComboBox);
            baselineTypeComboBox.setVisible(true);
            baselineTypeLabel.setVisible(true);
            drawBaselineCheckbox.setVisible(false);
            drawBaselineLabel.setVisible(false);
        }
        else if("historic data".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())){
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel,datePicker);
            setupDatePicker();
            baselineTypeComboBox.setVisible(false);
            baselineTypeLabel.setVisible(false);
            drawBaselineCheckbox.setVisible(true);
            drawBaselineLabel.setVisible(true);
        }
    }
    @FXML
    private void handleBaselineTypeAction(ActionEvent e) {
        if(typeComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("baseline")){
            fillInDaysOfWeek();
        }
        else if(typeComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("Historic data")){
            dayComboBox.getItems().clear();
        }
    }
    @FXML
    private void handleClearOnDrawAction(ActionEvent e) {
        if (clearCheckBox.isSelected()) {
            lineChart.getData().clear();
            handleStartAction(e);
        }
    }

    @FXML
    private void handleClearAction(ActionEvent e) {
        lineChart.getData().clear();
    }

    @FXML
    private void handleReverseRotuteAction(ActionEvent e) {
        try {
            String id = input.getId(idComboBox.getSelectionModel().getSelectedItem());
            if (id != null) {
                idComboBox.getSelectionModel().select(input.getReverse(id));
            }
        } catch (NullPointerException exception) {
            warn.setText("Nothing to reverse");
        }
    }
    @FXML
    private void handleDrawBaselineCheckboxAction(ActionEvent e) {
        //todo drawing baseline
    }

    @FXML
    private void handleSourceAction(ActionEvent e) {
        String notConnectedWarn = "Not connected to server!";
        if(sourceComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("server data")){
            if(!Connector.isConnectedToTheServer()){
                warn.setText(notConnectedWarn);
                warn.setVisible(true);
            }
            typeComboBox.setVisible(true);
            typeLabel.setVisible(true);
            typeComboBox.getItems().clear();
            typeComboBox.getItems().addAll("Baseline","Historic data");
        }
        else {
            if(warn.getText().equalsIgnoreCase(notConnectedWarn)){
                warn.setVisible(false);
            }
            typeLabel.setVisible(true);
            typeComboBox.setVisible(true);
            typeComboBox.getItems().clear();
            typeComboBox.getItems().addAll("Historic data");
        }
    }

    @FXML
    private void handleBackButtonAction(ActionEvent e) {
        parent.setScene();
    }
}
