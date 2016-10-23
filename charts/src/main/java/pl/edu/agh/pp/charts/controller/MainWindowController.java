package pl.edu.agh.pp.charts.controller;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.IOException;
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
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private LineChart<Number, Number> lineChart;
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
    private Label endDateLabel;
    @FXML
    private Label routeIdLabel;
    @FXML
    private Label routeDescLabel;



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
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    Connector.killAll();
                    Platform.exit();
                }
            });

            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            primaryStage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setScene(){
        primaryStage.setScene(scene);
    }
    public void updateAnomalyInfo(String screenId){

    }

    public void putAnomalyInfoOnScreen(String screenMessage) {
        Anomaly anomaly = AnomalyManager.getInstance().getAnomalyByScreenId(screenMessage);
        Platform.runLater(() -> {
            anomalyIdLabel.setText(anomaly.getAnomalyId());
            startDateLabel.setText(anomaly.getStartDate());
            endDateLabel.setText(anomaly.getEndDate());
            routeIdLabel.setText(anomaly.getRouteId());
            routeDescLabel.setText(anomaly.getRoute());
        } );
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
        text1.setFont(Font.font("Helvetica", FontPosture.REGULAR, 16));
//        putMessageOnScreen(text1);
    }

    public void addAnomalyToList(String text){
        Platform.runLater(() -> {
            anomaliesListView.getItems().add(0,text);
        } );
    }

    private String getLeverServerInfo(){
        //TODO keeping server info in Connector?
        return Connector.getLeverServerInfo();
    }

    private void setConnectedState(){
        if(connectedFlag){
            connectedLabel.setText(Connector.getAddressServerInfo());
            connectedLabel.setTextFill(Color.BLACK);
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
        }
        else {
            connectedLabel.setText("NOT CONNECTED");
            connectedLabel.setTextFill(Color.RED);
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
        }
    }

    private String formatDate(DateTime date){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
        return dtf.print(date);
    }

    @FXML
    private void initialize() throws IOException {
        putSystemMessageOnScreen("NOT CONNECTED",Color.RED);
        setConnectedState();
        currentLeverOnServer.setText(getLeverServerInfo());
        connectButton.setDefaultButton(true);
        try {
            System.out.println((String) Options.getInstance().getPreference("Server_Address", String.class));
            serverAddrTxtField.setText((String) Options.getInstance().getPreference("Server_Address", String.class));
            serverPortTxtField.setText((String) Options.getInstance().getPreference("Port", String.class));
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            illegalPreferenceObjectExpected.printStackTrace();
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
            if(!Pattern.matches("\\d*",port)){
                logger.error("Wrong server port pattern");
                serverPortTxtField.setStyle("-fx-text-box-border: red;");
                return;
            }
            else{
                serverPortTxtField.setStyle("-fx-text-box-border: black;");
            }
            if(port.equals(""))
                port = "7500";
            Connector.connect(address, port);
            connectedFlag = Connector.isConnectedToTheServer();
            if(!connectedFlag) putSystemMessageOnScreen("Failed to connect to " + Connector.getAddressServerInfo());
            setConnectedState();
//            putSystemMessageOnScreen("Connected to: " + Connector.getAddressServerInfo());
        } catch (Exception e1) {
            logger.error("Connecting error");
            e1.printStackTrace();
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
        }
    }

}


