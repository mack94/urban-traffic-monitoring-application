package pl.edu.agh.pp.charts.controller;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.input.Anomaly;
import pl.edu.agh.pp.charts.input.AnomalyManager;
import pl.edu.agh.pp.charts.input.Input;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.ServerOptions;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.charts.system.SystemRoutesInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Dawid on 2016-09-05.
 */
public class MainWindowController {

    private ObservableList<String> anomaliesList = FXCollections.observableArrayList();
    private Stage primaryStage = null;
    private Scene scene = null;
    private Input input;
    private ChartsController chartsController = null;
    private boolean connectedFlag = false;
    private WebEngine webEngine;
    private HtmlBuilder htmlBuilder;
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);
    private final AnomalyManager anomalyManager = AnomalyManager.getInstance();
    private final Options options = Options.getInstance();

    @FXML
    private volatile LineChart<Number, Number> lineChart;
    @FXML
    private WebView mapWebView;
    @FXML
    private Button chartsButton;
    @FXML
    private Label currentLeverOnServer;
    @FXML
    private Label connectedLabel;
    @FXML
    private TextField serverAddrTxtField;
    @FXML
    private TextField serverPortTxtField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private ListView<String> anomaliesListView;
    @FXML
    private Label anomalyIdLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label lastDateLabel;
    @FXML
    private Label routeIdLabel;
    @FXML
    private Label routeDescLabel;
    @FXML
    private Button saveDefaultButton;
    @FXML
    private Label recentDuration;
    @FXML
    private TextFlow systemMsgTextFlow;
    @FXML
    private Tab systemTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private Label leverValueLabel;
    @FXML
    private Label anomalyLiveTimeLabel;
    @FXML
    private Label BaselineWindowSizeLabel;
    @FXML
    private Label shiftLabel;
    @FXML
    private Label anomalyPortNrLabel;
    @FXML
    private Label anomaliesNumberLabel;
    @FXML
    private Button hideButton;
    @FXML
    private VBox hideBox;


    public MainWindowController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/MainWindow.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setTitle("Urban traffic monitoring - charts");
            scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(we -> {
                Connector.killAll();
                Platform.exit();
            });

            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
//            hidePane.managedProperty().bind(hidePane.visibleProperty());
            hideBox.managedProperty().bind(hideBox.visibleProperty());
            primaryStage.show();
        } catch (java.io.IOException e) {
            logger.error("exception while creating GUI " + e,e);
        }
    }

    public void setConnectedFlag(){
        this.connectedFlag = Connector.isConnectedToTheServer();
    }
    public void setScene(){
        primaryStage.setScene(scene);
    }
    public void updateAnomalyInfo(String screenId){
        if(anomaliesListView.getSelectionModel().getSelectedItem().equalsIgnoreCase(screenId)) {
            putAnomalyInfoOnScreen(screenId);
        }
    }

    public void putAnomalyInfoOnScreen(String screenMessage) {
        Anomaly anomaly = anomalyManager.getAnomalyByScreenId(screenMessage);
        Platform.runLater(() -> {
            anomalyIdLabel.setText(anomaly.getAnomalyId());
            startDateLabel.setText(anomaly.getStartDate());
            lastDateLabel.setText(anomaly.getLastDate());
            routeIdLabel.setText(anomaly.getRouteId());
            routeDescLabel.setText(anomaly.getRoute());
            recentDuration.setText(anomaly.getDuration());
            anomaliesNumberLabel.setText(anomaly.getAnomaliesNumber());
        } );
        putChartOnScreen(anomaly);
    }

    private void putChartOnScreen(Anomaly anomaly){
        //TODO thread safe
        Platform.runLater(() -> {
            if(lineChart != null) {
                if (lineChart.getData() != null) {
                    lineChart.getData().clear();
                }
                lineChart.setStyle(".default-color0.chart-series-line { -fx-stroke: red; }");
                lineChart.setStyle(".default-color1.chart-series-line { -fx-stroke: blue; }");
                XYChart.Series<Number, Number> series = anomalyManager.getChartData(anomaly);
                XYChart.Series<Number, Number> baseline = anomalyManager.getBaseline(anomaly);
                lineChart.getData().add(series);
                if(baseline != null) {
                    lineChart.getData().add(baseline);
                }
                createTooltips();
            }
        } );
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

    public void putAnomalyOnMap(String screenMessage) {
        Anomaly anomaly = AnomalyManager.getInstance().getAnomalyByScreenId(screenMessage);
        //TODO execution line below with route coordinates, map should then mark both points and center route start
        String startCoord = SystemRoutesInfo.getRouteCoordsStart(Integer.parseInt(anomaly.getRouteId()));
        String endCoord = SystemRoutesInfo.getRouteCoordsEnd(Integer.parseInt(anomaly.getRouteId()));
        String startLat = startCoord.split(",")[0];
        String startLng = startCoord.split(",")[1];
        String endLat = endCoord.split(",")[0];
        String endLng = endCoord.split(",")[1];
        webEngine.loadContent(htmlBuilder.loadMapStructure(startLat, startLng, endLat, endLng));
    }

    public void putSystemMessageOnScreen(String message) {
        putSystemMessageOnScreen(message,DateTime.now(),Color.BLACK);
    }

    public void putSystemMessageOnScreen(String message, Color color) {
        putSystemMessageOnScreen(message,DateTime.now(),color);
    }

    public void putSystemMessageOnScreen(String message, DateTime dateTime, Color color) {
        Text text1 = new Text(formatDate(dateTime) + "  " +message + "\n");
        text1.setFill(color);
        if(color == Color.RED) {
            Label lab = (Label)tabPane.getSelectionModel().getSelectedItem().getGraphic();
            if(lab == null || !lab.getText().equalsIgnoreCase("System info")){
                systemTab.getGraphic().setStyle("-fx-text-fill: red;");
            }
        }
        text1.setFont(Font.font("Helvetica", FontPosture.REGULAR, 16));
        Platform.runLater(() -> {
            systemMsgTextFlow.getChildren().add(0,new Text(" "));
            systemMsgTextFlow.getChildren().add(0,text1);
        } );
    }

    public void addAnomalyToList(String text){
        Platform.runLater(() -> {
            anomaliesListView.getItems().add(0,text);
        } );
    }

    public void removeAnomalyFromList(String screenMessage) {
        if(screenMessage == null){
            logger.error("No screen message");
            return;
        }
        if (anomaliesList.contains(screenMessage)){
            anomaliesList.remove(screenMessage);
        }
        else{
            logger.error("Trying to remove anomaly that doesn't exist");
        }
    }

    public void setConnectedLabel(String msg, Color color){
        Platform.runLater(() -> {
            connectedLabel.setText(msg);
            connectedLabel.setTextFill(Color.BLACK);
        });
    }

    public void setConnectedLabel(String msg){
        setConnectedLabel(msg,Color.BLACK);
    }

    private void setConnectedState(){
        if(connectedFlag){
            Platform.runLater(() -> {
                connectedLabel.setText(Connector.getAddressServerInfo());
                connectedLabel.setTextFill(Color.BLACK);
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
            });
        }
        else {
            Platform.runLater(() -> {
                connectedLabel.setText("NOT CONNECTED");
                connectedLabel.setTextFill(Color.RED);
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
            });
        }
    }

    private String formatDate(DateTime date){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
        return dtf.print(date);
    }

    public void reconnecting(){
        Connector.setIsFromConnecting(true);
        try {
            setConnectedLabel("Disconnected! Trying to reconnect",Color.RED);
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        int i = 0;
                        while (i<3 && !Connector.isConnectedToTheServer()) {
                            Connector.connect(Connector.getAddress(), Connector.getPort());
                            Thread.sleep(5000);
                            i++;
                        }
                        connectedFlag = Connector.isConnectedToTheServer();
                        setConnectedState();
                        Connector.getOptionsServerInfo();
                        Connector.setIsFromConnecting(false);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };

            new Thread(sleeper).start();

        } catch (Exception e) {
            logger.error("Connecting error " + e, e);
        }
    }


    private void setMapUp() {
        htmlBuilder = new HtmlBuilder();
        webEngine = mapWebView.getEngine();
        String defaultLat = "50.07";
        String defaultLng = "19.94";
        webEngine.loadContent(htmlBuilder.loadMapStructure(defaultLat, defaultLng, defaultLat, defaultLng));
    }

    public void updateServerInfo(ServerOptions options){
        Platform.runLater(() -> {
            leverValueLabel.setText(options.getLeverValue());
            anomalyLiveTimeLabel.setText(options.getAnomalyLiveTime());
            BaselineWindowSizeLabel.setText(options.getBaselineWindowSize());
            shiftLabel.setText(options.getShift());
            anomalyPortNrLabel.setText(options.getAnomalyPortNr());
        } );
    }

    @FXML
    private void initialize() throws IOException {
        systemTab.setGraphic(new Label("System info"));
        putSystemMessageOnScreen("NOT CONNECTED",Color.RED);
        systemTab.getGraphic().setStyle("-fx-text-fill: black;");
        setConnectedState();
        connectButton.setDefaultButton(true);
        setMapUp();
        try {
            serverAddrTxtField.setText((String) options.getPreference("Server_Address", String.class));
            serverPortTxtField.setText((String) options.getPreference("Server_Port", String.class));
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            logger.error("Options exception " + illegalPreferenceObjectExpected,illegalPreferenceObjectExpected);
        }
    }

    @FXML
    private void handleChartsButtonAction(ActionEvent e) {
        if (chartsController == null) {
            chartsController = new ChartsController(primaryStage, this);
            chartsController.show();
        }
        else{
            chartsController.setScene();
        }
    }

    @FXML
    private void handleConnectAction(ActionEvent e) {
        Connector.setIsFromConnecting(true);
        setConnectedLabel("connecting");
        try {
            String address = serverAddrTxtField.getText();
            if(!Pattern.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",address)){
                logger.error("Wrong server address pattern");
                serverAddrTxtField.setStyle("-fx-text-box-border: red;");
                return;
            }
            else{
                serverAddrTxtField.setStyle("-fx-text-box-border: black;");
            }
            String port = serverPortTxtField.getText();
            if(!Pattern.matches("\\d+",port)){
                logger.error("Wrong server port pattern");
                serverPortTxtField.setStyle("-fx-text-box-border: red;");
                return;
            }
            else{
                serverPortTxtField.setStyle("-fx-text-box-border: black;");
            }
            Connector.connect(address, port);
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Thread.sleep(5000);
                        connectedFlag = Connector.isConnectedToTheServer();
                        if(!connectedFlag) putSystemMessageOnScreen("Failed to connect to " + Connector.getAddressServerInfo(), Color.RED);
                        else putSystemMessageOnScreen("Connected to: " + Connector.getAddressServerInfo());
                        setConnectedState();
                        Connector.getOptionsServerInfo();
                        Connector.setIsFromConnecting(false);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };
            new Thread(sleeper).start();

        } catch (Exception e1) {
            logger.error("Connecting error " + e1, e1);
        }

    }

    @FXML
    private void handleDisconnectAction(ActionEvent e) {
        Connector.disconnect();
        connectedFlag = Connector.isConnectedToTheServer();
        if(connectedFlag) putSystemMessageOnScreen("Failed to disconnect from " + Connector.getAddressServerInfo());
        setConnectedState();
    }

    @FXML
    private void handleAnomalyClicked(MouseEvent e) {
        String selectedItem = anomaliesListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            putAnomalyInfoOnScreen(selectedItem);
            putAnomalyOnMap(selectedItem);
        }
    }
    @FXML
    private void handleSaveDefaultAction(ActionEvent e){
        HashMap<String,Object> map = new HashMap<>();
        map.put("Server_Address",serverAddrTxtField.getText());
        map.put("Server_Port",serverPortTxtField.getText());
        Options.getInstance().setPreferences(map);
    }
    @FXML
    private void handleTabChanged(){
        Label lab = (Label)tabPane.getSelectionModel().getSelectedItem().getGraphic();
        if(lab != null && lab.getText().equalsIgnoreCase("System info")){
            lab.setStyle("-fx-text-fill: black;");
        }
    }
    @FXML
    private  void  handleHideAction(){
        if(hideBox.isVisible()){
            hideBox.setVisible(false);
        }
        else {
            hideBox.setVisible(true);
        }
    }
    @FXML
    private void handleAnomalyPressed(KeyEvent e){
        String selectedItem = anomaliesListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            putAnomalyInfoOnScreen(selectedItem);
            putAnomalyOnMap(selectedItem);
        }
    }
}


