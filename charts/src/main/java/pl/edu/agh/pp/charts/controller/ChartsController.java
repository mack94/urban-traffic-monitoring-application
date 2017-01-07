package pl.edu.agh.pp.charts.controller;

import ch.qos.logback.classic.Logger;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.data.local.Input;
import pl.edu.agh.pp.charts.data.local.ResourcesHolder;
import pl.edu.agh.pp.charts.data.server.*;
import pl.edu.agh.pp.charts.parser.Parser;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class ChartsController {
    private Stage primaryStage = null;
    private boolean initialized = false;
    private Scene scene = null;
    private FileChooser fileChooser = null;
    private Parser parser;
    private Input input;
    private Set<Integer> idsSet = new HashSet<>();
    private ObservableList<String> localRouteIdList = FXCollections.observableArrayList();
    private ObservableList<String> serverRouteIdList = FXCollections.observableArrayList();
    private ObservableList<String> serverDatesList = FXCollections.observableArrayList();
    private MainWindowController parent;
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Button fileButton;
    @FXML
    private Button startButton;
    @FXML
    private Label warn;
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
    //    @FXML
//    private Button reverseRouteButton;
    @FXML
    private Label logsListLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label dayLabel;
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
    @FXML
    private Label drawAnomaliesLabel;
    @FXML
    private CheckBox drawAnomaliesCheckbox;
    @FXML
    private Label clearLabel;


    public ChartsController(Stage primaryStage, MainWindowController parent) {
        this.primaryStage = primaryStage;
        this.parent = parent;
        Connector.setChartsController(this);
    }

    void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/Charts.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setOnCloseRequest(we -> {
                if (input != null) {
                    input.cleanUp();
                }
            });
            scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            primaryStage.setScene(scene);
            initialized = true;
//            primaryStage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    void setScene() {
        primaryStage.setScene(scene);
    }


    public void setServerRouteIds() {
        serverRouteIdList.clear();
        List<String> routes = ServerRoutesInfo.getRoutes();
        for (String r : routes)
            serverRouteIdList.add(r);
    }

    public void setServerDates() {
        serverDatesList.clear();
        Map<String, List<Integer>> map = ServerDatesInfo.getDates();
        if (map != null)
            serverDatesList.addAll(map.keySet());
    }

    boolean isInitialized() {
        return initialized;
    }

    /**
     * If the class was not initialized or source is set to Local data this method does nothing and returns value true.
     * <p>
     * If source is set to Server data method checks whether Client is currently connected to the server.
     * If it is not then sets warning Label. If it is connected and warning label was set to "Not connected to server!"
     * the warning label is cleared.
     *
     * @return true if connected to server or source set to Local data, false otherwise
     */
    boolean checkConnection() {
        if (isInitialized() && "server data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
            if (!Connector.isConnectedToTheServer()) {
                Platform.runLater(() -> warn.setText("Not connected to server!"));
                return false;
            } else if ("Not connected to server!".equalsIgnoreCase(warn.getText())) {
                Platform.runLater(() -> warn.setText(""));
            }
        }
        return true;
    }

    private void setupFields() {
//        localRouteIdList.clear();
        if (sourceComboBox.getSelectionModel().getSelectedItem() != null && sourceComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("local data")) {
            idComboBox.setItems(localRouteIdList);
        } else if (sourceComboBox.getSelectionModel().getSelectedItem() != null && sourceComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("server data")) {
            Connector.setServerAvailableRouteIds();
            idComboBox.setItems(serverRouteIdList);
        }
        setupDatePicker();
    }

    private void fillInDaysOfWeek() {
        dayComboBox.getItems().clear();
        dayComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
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
                Tooltip.install(d.getNode(), new Tooltip(s.getName() + "\nTime of the day: " + iPart + "h " + (long) (fPart * 60) + "min" + "\nDuration: " + d.getYValue().toString() + " seconds"));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void setupDatePicker() {
        datePicker = new DatePicker();
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker tmoDatePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                if (!availableDate(date))
                                    setDisable(true);
                            }
                        };
                    }
                };
        datePicker.setDayCellFactory(dayCellFactory);
        String type = typeComboBox.getSelectionModel().getSelectedItem();
        if ("historical data".equalsIgnoreCase(type) || "historical anomalies".equalsIgnoreCase(type)) {
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel, datePicker);
        }
        datePicker.setOnAction(event -> {
            LocalDate date = datePicker.getValue();
            Map<String, List<Integer>> map = ServerDatesInfo.getDates();
            List<Integer> routes = map.get(date.toString());
            ObservableList<String> dateRouteIdList = FXCollections.observableArrayList();
            for (Integer id : routes) {
                dateRouteIdList.add(String.valueOf(id) + " " + ServerRoutesInfo.getRouteName(id));
            }
            idComboBox.setItems(dateRouteIdList);
        });

    }

    private boolean availableDate(LocalDate date) {
        if ("local data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
            return input.getDays().contains((date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth()));
        } else {
            return serverDatesList.contains(date.toString());
        }
    }

    private void initializeFields() {
        setupFileChooser();
        datePicker = new DatePicker();
        dayComboBox = new ComboBox<>();
        dayLabel = new Label("Day");
        setupVisibility();
        warn.setStyle("-fx-text-fill: red");

        Image reverseButtonImage = new Image(Main.class.getResourceAsStream("/reverse.png"));
//        reverseRouteButton.setGraphic(new ImageView(reverseButtonImage));

        fillInDaysOfWeek();

        sourceComboBox.getItems().addAll("Local Data", "Server Data");
    }

    private void setupFileChooser() {
        fileChooser = new FileChooser();
        File file = new File("./");
        fileChooser.setInitialDirectory(file);
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log Files", "*.log"));

    }

    private void setupVisibility() {
        clearCheckBox.setVisible(false);
        clearLabel.setVisible(false);
        startButton.setDefaultButton(true);
        clearCheckBox.setSelected(true);
        typeComboBox.setVisible(false);
        typeLabel.setVisible(false);
        dayComboBox.setVisible(false);
        idComboBox.setVisible(false);
        idLabel.setVisible(false);
//        reverseRouteButton.setVisible(false);
        drawBaselineCheckbox.setVisible(false);
        drawBaselineLabel.setVisible(false);
        drawAnomaliesCheckbox.setVisible(false);
        drawAnomaliesLabel.setVisible(false);
    }

    private void setupChart() {
        lineChart.setTitle("");
        lineChart.setAnimated(false);
//        lineChart.setLegendVisible(false);
        //Panning works via either secondary (right) mouse or primary with ctrl held down
        ChartPanManager panner = new ChartPanManager(lineChart);
        panner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY &&
                    (mouseEvent.getButton() != MouseButton.PRIMARY ||
                            !mouseEvent.isShortcutDown()))
                mouseEvent.consume();
        });
        panner.start();

        //Zooming works only via primary mouse button without ctrl held down
        JFXChartUtil.setupZooming(lineChart, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.PRIMARY ||
                    mouseEvent.isShortcutDown())
                mouseEvent.consume();
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(lineChart);
    }

    @FXML
    private void initialize() {
        Locale.setDefault(Locale.ENGLISH);
        initializeFields();
        setupChart();
        parser = new Parser();
        input = new Input();
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
    }

    @FXML
    private void handleStartAction(ActionEvent e) {
        String source = sourceComboBox.getSelectionModel().getSelectedItem();
        if (source != null && source.equalsIgnoreCase("local data")) {
            drawLocalData();
        } else {
            drawServerData();
        }
    }

    private void drawLocalData() {
        if (idComboBox.getSelectionModel().getSelectedItem() == null
                || typeComboBox.getSelectionModel().getSelectedItem() == null) {
            if ("historical data".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
                if (dayComboBox.getSelectionModel().getSelectedItem() == null) {
                    warn.setText("Select all parameters");
                    return;
                }
            } else {
                if (datePicker.getValue() == null) {
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
        if ("current baselines".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            day = dayComboBox.getSelectionModel().getSelectedItem();
        } else {
            day = datePicker.getValue().toString();
        }
        String id = input.getId(idComboBox.getSelectionModel().getSelectedItem());
        Map<Double, Double> trafficValues = null;
        if ("historical data".equalsIgnoreCase(type)) {
            trafficValues = input.getData(day, id, true, false);
        } else if (type.equals("Aggregated day of week")) {
            day = day.substring(0, 3).toUpperCase();
            trafficValues = input.getData(day, id, true, true);
        }
        if (trafficValues != null)
            for (Double key : trafficValues.keySet()) {
                seriesDurationInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + idComboBox.getSelectionModel().getSelectedItem());
                seriesDurationInTraffic.getData().add(new XYChart.Data<>(key, trafficValues.get(key)));
            }

        lineChart.getData().add(seriesDurationInTraffic);
        lineChart.getData().add(seriesDuration);

        createTooltips();
    }

    private void drawServerData() {
        if (!checkConnection()) return;
        String type = typeComboBox.getSelectionModel().getSelectedItem();
        String route = idComboBox.getSelectionModel().getSelectedItem();
        String id = ServerRoutesInfo.getId(route);
        String dayForHistoricalData = null;
        if (datePicker.getValue() != null)
            dayForHistoricalData = datePicker.getValue().toString();
        String dayForBaseline = dayComboBox.getSelectionModel().getSelectedItem();
        if (type == null || id == null) {
            warn.setText("Select all parameters");
            return;
        }
        if (type.equalsIgnoreCase("current baselines") && dayForBaseline == null) {
            warn.setText("Select all parameters");
            return;
        } else if ((type.equalsIgnoreCase("historical data") || type.equalsIgnoreCase("historical anomalies")) && dayForHistoricalData == null) {
            warn.setText("Select all parameters");
            return;
        }

        warn.setText("");
        if (clearCheckBox.isSelected()) {
            lineChart.getData().clear();
        }
        if (type.equalsIgnoreCase("current baselines")) {
            drawBaseline(id, String.valueOf(DayOfWeek.valueOf(dayForBaseline.toUpperCase()).getValue()), "");
        } else if (type.equalsIgnoreCase("historical data")) {
            drawHistoricalData(id, dayForHistoricalData);
        } else if (type.equalsIgnoreCase("historical anomalies")) {
            drawHistoricalAnomalies(id, dayForHistoricalData);
        }
    }

    private void drawHistoricalData(final String id, String dayForHistoricalData) {
        HistoricalData historicalData = HistoricalDataManager.getHistoricalData(Integer.valueOf(id), DateTime.parse(dayForHistoricalData));
        if (historicalData == null) {
            Connector.demandHistoricalData(DateTime.parse(dayForHistoricalData), Integer.valueOf(id));
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        HistoricalData historicalData = null;
                        int i = 0;
                        while (i < 3 && historicalData == null) {
                            historicalData = HistoricalDataManager.getHistoricalData(Integer.valueOf(id), DateTime.parse(dayForHistoricalData));
                            Thread.sleep(1000);
                            i++;
                        }
                        if (historicalData == null) {
                            logger.error("Did not get the historical after demand");
                            Platform.runLater(() -> warn.setText("Server did not respond"));
                        } else {
                            logger.info("got a response, historical data found!");
                            Platform.runLater(() -> {
                                warn.setText("");
                                lineChart.getData().add(HistoricalDataManager.getHistoricalData(Integer.valueOf(id), DateTime.parse(dayForHistoricalData)).getHistoricalDataSeries());
                                createTooltips();
                            });
                        }
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };
            new Thread(sleeper).start();
        } else {
            Platform.runLater(() -> lineChart.getData().add(historicalData.getHistoricalDataSeries()));
            createTooltips();
        }
        if (drawBaselineCheckbox.isSelected() && !drawAnomaliesCheckbox.isSelected()) {
            drawBaseline(id, String.valueOf(DateTime.parse(dayForHistoricalData).dayOfWeek().get()), dayForHistoricalData);
        }
        if (drawAnomaliesCheckbox.isSelected()) {
            drawHistoricalAnomalies(id, dayForHistoricalData);
        }
    }

    private void drawBaseline(final String id, final String dayForBaseline, String type) {
        Baseline baseline = BaselineManager.getBaseline(Integer.valueOf(id), DayOfWeek.of(Integer.valueOf(dayForBaseline)), type);
        if (baseline == null) {
            Connector.demandBaseline(DayOfWeek.of(Integer.valueOf(dayForBaseline)), Integer.valueOf(id), type);
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Baseline baseline = null;
                        int i = 0;
                        while (i < 3 && baseline == null) {
                            baseline = BaselineManager.getBaseline(Integer.valueOf(id), DayOfWeek.of(Integer.valueOf(dayForBaseline)), type);
                            Thread.sleep(1000);
                            i++;
                        }
                        if (baseline == null) {
                            logger.error("Did not get the baseline after demand");
                            Platform.runLater(() -> warn.setText("Server did not respond"));
                        } else {
                            logger.info("Got a response, baseline found!");
                            Platform.runLater(() -> {
                                warn.setText("");
                                lineChart.getData().add(BaselineManager.getBaseline(Integer.valueOf(id), DayOfWeek.of(Integer.valueOf(dayForBaseline)), type).getBaselineSeries());
                                createTooltips();
                            });
                        }
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };
            new Thread(sleeper).start();
        } else {
            Platform.runLater(() -> lineChart.getData().add(baseline.getBaselineSeries()));
            createTooltips();
        }
    }

    private void drawHistoricalAnomalies(final String id, final String dayForHistoricalAnomalies) {
        //TODO
        if (drawBaselineCheckbox.isSelected()) {
            drawBaseline(id, String.valueOf(DateTime.parse(dayForHistoricalAnomalies).dayOfWeek().get()), dayForHistoricalAnomalies);
        }
        HistoricalAnomaly historicalAnomaly = HistoricalAnomalyManager.getHistoricalAnomalies(Integer.valueOf(id), DateTime.parse(dayForHistoricalAnomalies));
        if (historicalAnomaly == null) {
            Connector.demandHistoricalAnomalies(DateTime.parse(dayForHistoricalAnomalies), Integer.valueOf(id));
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        HistoricalAnomaly historicalAnomaly = null;
                        int i = 0;
                        while (i < 3 && historicalAnomaly == null) {
                            historicalAnomaly = HistoricalAnomalyManager.getHistoricalAnomalies(Integer.valueOf(id), DateTime.parse(dayForHistoricalAnomalies));
                            Thread.sleep(1000);
                            i++;
                        }
                        if (historicalAnomaly == null) {
                            logger.error("Did not get the historical anomalies after demand");
                            Platform.runLater(() -> warn.setText("Server did not respond"));
                        } else {
                            logger.info("got a response, historical anomalies found!");
                            Platform.runLater(() -> {
                                warn.setText("");
                                final HistoricalAnomaly historicalAnomaliesContainer = HistoricalAnomalyManager.getHistoricalAnomalies(Integer.valueOf(id), DateTime.parse(dayForHistoricalAnomalies));
                                if (historicalAnomaliesContainer != null) {
                                    for (HistoricalAnomaly ha : historicalAnomaliesContainer.getAnomalies()) {
                                        lineChart.getData().add(ha.getHistoricalAnomalySeries());
                                    }
                                    setAnomalyChartStyles();
                                    createTooltips();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };
            new Thread(sleeper).start();
        } else {
            Platform.runLater(() -> {
                final HistoricalAnomaly historicalAnomaliesContainer = HistoricalAnomalyManager.getHistoricalAnomalies(Integer.valueOf(id), DateTime.parse(dayForHistoricalAnomalies));
                if (historicalAnomaliesContainer != null) {
                    for (HistoricalAnomaly ha : historicalAnomaliesContainer.getAnomalies()) {
                        lineChart.getData().add(ha.getHistoricalAnomalySeries());
                    }
                    setAnomalyChartStyles();
                    createTooltips();
                }
            });
        }
    }

    private void setAnomalyChartStyles() {
        Legend legend = (Legend) findNode(lineChart, Legend.class.getName(), "chart-legend");
        for (XYChart.Series<Number, Number> series : lineChart.getData()) {
            if (series.getName().contains("Anomaly")) {
                for (Legend.LegendItem legendItem : legend.getItems()) {
                    if (legendItem.getText().contains("Anomaly")) {
                        legendItem.getSymbol().setStyle("-fx-background-color: red,white;");
                    }
                }
                series.nodeProperty().get().setStyle("-fx-stroke-width: 5px; -fx-stroke: red; ");
                for (XYChart.Data<Number, Number> d : series.getData()) {
                    double num = (double) d.getXValue();
                    long iPart;
                    double fPart;
                    iPart = (long) num;
                    fPart = num - iPart;
//                  d.nodeProperty().get().setStyle("-fx-background-color: red,white;");
                    d.nodeProperty().get().getStyleClass().add("AnomalyNode");
                    Tooltip.install(d.getNode(), new Tooltip(series.getName() + "\n Time of the day: " + iPart + "h " + (long) (fPart * 60) + "min" + "\nDuaration: " + d.getYValue().toString() + " seconds"));
                    d.nodeProperty().get().setOnMouseEntered(event -> {
                        d.getNode().getStyleClass().remove("AnomalyNode");
                        d.getNode().getStyleClass().add("onHover");
                    });
                    d.nodeProperty().get().setOnMouseExited(event -> {
                        d.getNode().getStyleClass().add("AnomalyNode");
                        d.getNode().getStyleClass().remove("onHover");
                    });
                }
            } else if (series.getName().contains("Baseline")) {
                series.nodeProperty().get().setStyle("-fx-stroke-width: 2px; -fx-stroke: green;");

                for (Legend.LegendItem legendItem : legend.getItems()) {
                    if (legendItem.getText().contains("Baseline")) {
                        legendItem.getSymbol().setStyle("-fx-background-color: green,white;");
                    }
                }
                for (XYChart.Data<Number, Number> d : series.getData()) {
                    double num = (double) d.getXValue();
                    long iPart;
                    double fPart;
                    iPart = (long) num;
                    fPart = num - iPart;
                    d.nodeProperty().get().getStyleClass().add("BaselineDataNode");
                    Tooltip.install(d.getNode(), new Tooltip(series.getName() + "\n Time of the day: " + iPart + "h " + (long) (fPart * 60) + "min" + "\nDuaration: " + d.getYValue().toString() + " seconds"));
                    d.nodeProperty().get().setOnMouseEntered(event -> {
                        d.getNode().getStyleClass().remove("BaselineDataNode");
                        d.getNode().getStyleClass().add("onHover");
                    });
                    d.nodeProperty().get().setOnMouseExited(event -> {
                        d.getNode().getStyleClass().add("BaselineDataNode");
                        d.getNode().getStyleClass().remove("onHover");
                    });
                }
            } else {
                for (Legend.LegendItem legendItem : legend.getItems()) {
                    if (!legendItem.getText().contains("Anomaly") && !legendItem.getText().contains("Baseline")) {
                        legendItem.getSymbol().setStyle("-fx-background-color: blue,white;");
                    }
                }
                series.nodeProperty().get().setStyle("-fx-stroke-width: 2px; -fx-stroke: blue;");
                for (XYChart.Data<Number, Number> d : series.getData()) {
                    double num = (double) d.getXValue();
                    long iPart;
                    double fPart;
                    iPart = (long) num;
                    fPart = num - iPart;
                    d.nodeProperty().get().getStyleClass().add("HistoricalDataNode");
                    Tooltip.install(d.getNode(), new Tooltip(series.getName() + "\n Time of the day: " + iPart + "h " + (long) (fPart * 60) + "min" + "\nDuaration: " + d.getYValue().toString() + " seconds"));
                    d.nodeProperty().get().setOnMouseEntered(event -> {
                        d.getNode().getStyleClass().remove("HistoricalDataNode");
                        d.getNode().getStyleClass().add("onHover");
                    });
                    d.nodeProperty().get().setOnMouseExited(event -> {
                        d.getNode().getStyleClass().add("HistoricalDataNode");
                        d.getNode().getStyleClass().remove("onHover");
                    });
                }
            }
        }
    }

    private static Node findNode(final Parent aParent, final String aClassname, final String aStyleName) {
        if (null != aParent) {
            final ObservableList<Node> children = aParent.getChildrenUnmodifiable();
            if (null != children) {
                for (final Node child : children) {
                    String className = child.getClass().getName();
                    if (className.contains("$")) {
                        className = className.substring(0, className.indexOf("$"));
                    }
                    if (0 == aClassname.compareToIgnoreCase(className)) {
                        if ((null == aStyleName) || (0 == aStyleName.length())) {
                            return child;
                        } else {
                            final String styleName = child.getStyleClass().toString();
                            if (0 == aStyleName.compareToIgnoreCase(styleName)) {
                                return child;
                            }
                        }
                    }
                    if (child instanceof Parent) {
                        final Node node = findNode((Parent) child, aClassname, aStyleName);

                        if (null != node) {
                            return node;
                        }
                    }
                }
            }
        }

        return null;
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

        if (type.equals("Exact date")) {
            if (route == 4) {
                summaryTraffic = input.getSummary(day, 1, 3, true, false);
                traffic = input.getData(day, String.valueOf(route), true, false);
            } else if (route == 8) {
                summaryTraffic = input.getSummary(day, 5, 7, true, false);
                traffic = input.getData(day, String.valueOf(route), true, false);
            }
        } else if (type.equals("Aggregated day of week")) {
            day = day.substring(0, 3).toUpperCase();
            if (route == 4) {
                summaryTraffic = input.getSummary(day, 1, 3, true, true);
                traffic = input.getData(day, String.valueOf(route), true, true);
            } else if (route == 8) {
                summaryTraffic = input.getSummary(day, 5, 7, true, true);
                traffic = input.getData(day, String.valueOf(route), true, true);
            }
        }

        String ids = route == 4 ? "1-3" : "5-7";
        if (summaryTraffic != null) {
            for (Double key : summaryTraffic.keySet()) {
                seriesDurationSummaryInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + ids);
                seriesDurationSummaryInTraffic.getData().add(new XYChart.Data<>(key, summaryTraffic.get(key)));
            }
        }

        if (traffic != null) {
            for (Double key : traffic.keySet()) {
                seriesDurationInTraffic.setName("Duration in traffic - Day: " + day + ", ID: " + route);
                seriesDurationInTraffic.getData().add(new XYChart.Data<>(key, traffic.get(key)));
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
//        reverseRouteButton.setVisible(true);
        if ("current baselines".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel, dayComboBox);
            dayHBox.setVisible(true);
            drawBaselineCheckbox.setVisible(false);
            drawBaselineLabel.setVisible(false);
            drawAnomaliesCheckbox.setVisible(false);
            drawAnomaliesLabel.setVisible(false);
        } else if ("historical data".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel, datePicker);
            dayHBox.setVisible(true);
            setupDatePicker();
            if ("server data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
                drawBaselineCheckbox.setVisible(true);
                drawBaselineLabel.setVisible(true);
                drawAnomaliesCheckbox.setVisible(true);
                drawAnomaliesLabel.setVisible(true);
            }
        } else if ("historical anomalies".equalsIgnoreCase(typeComboBox.getSelectionModel().getSelectedItem())) {
            dayHBox.getChildren().clear();
            dayHBox.getChildren().addAll(dayLabel, datePicker);
            dayHBox.setVisible(true);
            setupDatePicker();
            drawBaselineCheckbox.setVisible(true);
            drawBaselineLabel.setVisible(true);
            drawAnomaliesCheckbox.setVisible(false);
            drawAnomaliesLabel.setVisible(false);
        }
    }

    @FXML
    private void handleBaselineTypeAction(ActionEvent e) {
        dayHBox.setVisible(true);
        if (typeComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("current baselines")) {
            fillInDaysOfWeek();
        } else if (typeComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("Historical data")) {
            dayComboBox.getItems().clear();
            //TODO historical
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
    private void handleReverseRouteAction(ActionEvent e) {
        try {
            if ("local data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
                String id = input.getId(idComboBox.getSelectionModel().getSelectedItem());
                if (id != null) {
                    idComboBox.getSelectionModel().select(input.getReverse(id));
                }
            }
            if ("server data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {

            }
        } catch (NullPointerException exception) {
            warn.setText("Nothing to reverse");
        }
    }

    @FXML
    private void handleSourceAction(ActionEvent e) {
        setupFields();
        checkConnection();
        if ("server data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
            Platform.runLater(() -> {
                typeComboBox.setVisible(true);
                typeLabel.setVisible(true);
                clearLabel.setVisible(true);
                clearCheckBox.setVisible(true);
                typeComboBox.getItems().clear();
                typeComboBox.getItems().addAll("Current baselines", "Historical data", "Historical anomalies");
            });
        } else if ("local data".equalsIgnoreCase(sourceComboBox.getSelectionModel().getSelectedItem())) {
            Platform.runLater(() -> {
                typeComboBox.getItems().clear();
                typeComboBox.getItems().addAll("Historical data");
                typeLabel.setVisible(true);
                typeComboBox.setVisible(true);
//                reverseRouteButton.setVisible(false);
                drawAnomaliesCheckbox.setVisible(false);
                drawBaselineCheckbox.setVisible(false);
                drawBaselineLabel.setVisible(false);
                drawAnomaliesLabel.setVisible(false);
                dayHBox.setVisible(false);
                idComboBox.setVisible(false);
                idLabel.setVisible(false);
                clearLabel.setVisible(true);
                clearCheckBox.setVisible(true);
            });
        }
    }

    @FXML
    private void handleBackButtonAction(ActionEvent e) {
        parent.setScene();
    }


}
