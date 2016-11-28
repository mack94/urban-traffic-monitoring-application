package pl.edu.agh.pp.charts.controller;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.data.server.Anomaly;
import pl.edu.agh.pp.charts.data.server.AnomalyManager;
import pl.edu.agh.pp.charts.data.server.ServerDatesInfo;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Dawid on 2016-09-05.
 */
public class MainWindowController {

    private Stage primaryStage = null;
    private Scene scene = null;
    private ChartsController chartsController = null;
    private boolean connectedFlag = false;
    private final Logger logger = (Logger) LoggerFactory.getLogger(MainWindowController.class);
    private final AnomalyManager anomalyManager = AnomalyManager.getInstance();
    private final Options options = Options.getInstance();
    private boolean redrawThreadCreated = false;

    @FXML
    private volatile LineChart<Number, Number> lineChart;
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
    private ListView<HBox> anomaliesListView;
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
    private Button hideServerSettingsButton;
    @FXML
    private Button hideAnomaliesButton;
    @FXML
    private VBox hideBox;
    @FXML
    private VBox anomaliesVBox;
    @FXML
    private Label previousDurationLabel;
    @FXML
    private Label ExcessLabel;
    @FXML
    private Label trendLabel;
    @FXML
    private VBox leverBox;
    @FXML
    private Label leverValueLabelText;
    @FXML
    private Label anomalyLiveTimeLabelText;
    @FXML
    private Label BaselineWindowSizeLabelText;
    @FXML
    private Label shiftLabelText;
    @FXML
    private Label anomalyPortNrLabelText;
    @FXML
    private Label serverAddrLabel;
    @FXML
    private Label serverPortLabel;
    @FXML
    private LineChart<Number, Number> allAnomaliesLineChart;
    @FXML
    private GridPane anomaliesGridPane;
    @FXML
    private Label anomaliesListLabel;
    @FXML
    private HBox anomaliesListHBox;
    @FXML
    private ComboBox<String> monitoredRoutesComboBox;
    @FXML
    private Button resetDefaultButton;


    public MainWindowController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/MainWindow.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setTitle("©UTM - Cracow Urban Traffic Monitoring");
            scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(we -> {
                Connector.killAll();
                Platform.exit();
            });

            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            hideBox.managedProperty().bind(hideBox.visibleProperty());
            anomaliesListView.managedProperty().bind(anomaliesListView.visibleProperty());
            anomaliesListLabel.managedProperty().bind(anomaliesListLabel.visibleProperty());;
            anomaliesListHBox.managedProperty().bind(anomaliesListHBox.visibleProperty());
            anomaliesGridPane.managedProperty().bind(anomaliesGridPane.visibleProperty());
            tabPane.managedProperty().bind(tabPane.visibleProperty());
            primaryStage.show();
        } catch (java.io.IOException e) {
            logger.error("exception while creating GUI " + e,e);
        }
    }

    public void setConnectedFlag(){
        this.connectedFlag = Connector.isConnectedToTheServer();
    }
    void setScene(){
        primaryStage.setScene(scene);
    }
    public void updateAnomalyInfo(String anomalyId){
        updateAnomalyList(anomalyId);
        if("anomalies summary chart".equalsIgnoreCase(tabPane.getSelectionModel().getSelectedItem().getText())) {
            redrawAllAnomaliesChart();
        }
        if(anomalyId != null && anomalyId.equalsIgnoreCase(getSelectedAnomalyId())) {
            putAnomalyInfoOnScreen(anomalyId);
        }
    }
    private void putAnomalyInfoOnScreen(String anomalyId) {
        Anomaly anomaly = anomalyManager.getAnomalyById(anomalyId);
        if(anomaly == null ){
            return;
        }
        Platform.runLater(() -> {
            anomalyIdLabel.setText(anomaly.getAnomalyId());
            startDateLabel.setText(anomaly.getStartDate());
            lastDateLabel.setText(anomaly.getLastDate());
            routeIdLabel.setText(anomaly.getRouteId());
            routeDescLabel.setText(anomaly.getRoute());
            recentDuration.setText(anomaly.getDuration());
            anomaliesNumberLabel.setText(anomaly.getAnomaliesNumber());
            previousDurationLabel.setText(anomaly.getPreviousDuration());
            ExcessLabel.setText(anomaly.getPercent());
            trendLabel.setText(anomaly.getTrend());
        } );
        putChartOnScreen(anomaly);
    }

    private void updateAnomalyList(String anomalyId){
        Anomaly anomaly = anomalyManager.getAnomalyById(anomalyId);
        for(HBox hbox: anomaliesListView.getItems()){
            if(hbox.getId().equalsIgnoreCase(anomalyId)){
                Platform.runLater(() -> {
                    ((Label)((Pane)hbox.getChildren().get(3)).getChildren().get(0)).setText(anomaly.getPercent());
                    ((Label)((Pane)hbox.getChildren().get(4)).getChildren().get(0)).setText(anomaly.getTrend());
                    anomaliesListView.getItems().sort((o1, o2) -> {
                        if (Integer.parseInt(((Label) ((Pane) o1.getChildren().get(3)).getChildren().get(0)).getText()) < Integer.parseInt(
                                ((Label) ((Pane) o2.getChildren().get(3)).getChildren().get(0)).getText())) return 1;
                        else if (Integer.parseInt(((Label) ((Pane) o1.getChildren().get(3)).getChildren().get(0)).getText()) > Integer.parseInt(
                                ((Label) ((Pane) o2.getChildren().get(3)).getChildren().get(0)).getText())) return -1;
                        else return 0;
                    });
                });
            }
        }
    }

    private void clearInfoOnScreen() {
        Platform.runLater(() -> {
            anomalyIdLabel.setText("");
            startDateLabel.setText("");
            lastDateLabel.setText("");
            routeIdLabel.setText("");
            routeDescLabel.setText("");
            recentDuration.setText("");
            anomaliesNumberLabel.setText("");
            previousDurationLabel.setText("");
            ExcessLabel.setText("");
            trendLabel.setText("");
            if(!lineChart.getData().isEmpty())
                lineChart.getData().clear();
                lineChart.setTitle("Selected anomaly chart");
        } );
    }

    private void putChartOnScreen(Anomaly anomaly){
        Platform.runLater(() -> {
            lineChart.setId("Chart" + anomaly.getRouteId());
            lineChart.setTitle(anomaly.getRoute()+" anomaly "+ anomaly.getAnomalyId() + " chart");
            if(lineChart != null) {
                if (lineChart.getData() != null) {
                    lineChart.getData().clear();
                }
                XYChart.Series<Number, Number> series = anomalyManager.getChartData(anomaly);
                series.setName("Anomaly " + anomaly.getAnomalyId());
                XYChart.Series<Number, Number> baseline = anomalyManager.getBaseline(anomaly);
                String dayName = DayOfWeek.of(Integer.parseInt(anomaly.getDayOfWeek())).name();
                lineChart.getData().add(series);
                if(baseline != null) {
                    baseline.setName("Baseline - " + dayName.substring(0,1) + dayName.substring(1).toLowerCase());
                    lineChart.getData().add(baseline);
                }
                createChartTooltips();
            }
        } );
    }

    public void redrawAllAnomaliesChart(){
        if(!redrawThreadCreated) {
            redrawThreadCreated = true;
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Thread.sleep(5000);
                        if(allAnomaliesLineChart != null && allAnomaliesLineChart.getData() != null && !allAnomaliesLineChart.getData().isEmpty()){
                            Platform.runLater(() -> allAnomaliesLineChart.getData().clear());
                        }
                        drawAnomaliesSummaryChart();
                        redrawThreadCreated = false;
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception");
                    }
                    return null;
                }
            };
            new Thread(sleeper).start();
        }
    }

    private void drawAnomaliesSummaryChart(){
        for(HBox hbox: anomaliesListView.getItems()){
            Platform.runLater(() -> allAnomaliesLineChart.getData().add(anomalyManager.getPercentChartData(hbox.getId())));
        }
    }

    private void createChartTooltips() {
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

    public void addAnomalyToList(Anomaly anomaly){
        putAnomalyOnList(anomaly.getAnomalyId(),anomaly.getRouteId(),anomaly.getRoute(),anomaly.getStartDate(),anomaly.getPercent(),anomaly.getTrend());
    }

    private void putAnomalyOnList(String anomalyID,String routeID, String routeName, String startDate, String excess, String Trend){
        HBox hBox = new HBox();
        hBox.setId(anomalyID);
        hBox.getChildren().addAll(addLabel(routeID,50),addLabel(routeName,300),addLabel(startDate,150),addLabel(excess,50),addLabel(Trend,50));

        int i;
        for(i = 0; i<anomaliesListView.getItems().size(); i++){
            String stringExcess = ((Label)((Pane)anomaliesListView.getItems().get(i).getChildren().get(3)).getChildren().get(0)).getText();
            if(!"".equals(stringExcess) || Integer.parseInt(stringExcess) < Integer.parseInt(excess)) break;
        }
        int finalI = i;
        Platform.runLater(() -> {
            anomaliesListView.getItems().add(finalI,hBox);
            anomaliesListView.getItems().sort((o1, o2) -> {
                if (Integer.parseInt(((Label) ((Pane) o1.getChildren().get(3)).getChildren().get(0)).getText()) < Integer.parseInt(
                        ((Label) ((Pane) o2.getChildren().get(3)).getChildren().get(0)).getText())) return 1;
                else if (Integer.parseInt(((Label) ((Pane) o1.getChildren().get(3)).getChildren().get(0)).getText()) > Integer.parseInt(
                        ((Label) ((Pane) o2.getChildren().get(3)).getChildren().get(0)).getText())) return -1;
                else return 0;
            });
        });

    }

    private String getSelectedAnomalyId(){
        HBox hBox = anomaliesListView.getSelectionModel().getSelectedItem();
        if(hBox != null) return hBox.getId();
        return "";
    }

    private boolean isAnomalyOnScreen(String anomalyId){
        for(HBox hbox: anomaliesListView.getItems()){
            if(hbox.getId().equalsIgnoreCase(anomalyId))
                return true;
        }
        return false;
    }

    private Pane addLabel(String txt,double width){
        Pane pane = new Pane();
        pane.setId("listLine");
        Label label = new Label(txt);
        label.setPrefWidth(width);
        label.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(label);
        return pane;
    }

    public void removeAnomalyFromList(String anomalyId) {
        if(anomalyId == null){
            logger.error("No anomaly ID");
            return;
        }
        if (isAnomalyOnScreen(anomalyId)){
            for(HBox hbox: anomaliesListView.getItems()){
                if(hbox.getId().equalsIgnoreCase(anomalyId)){
                    Platform.runLater(() -> {
                        anomaliesListView.getItems().remove(hbox);
                        redrawAllAnomaliesChart();
                        if(anomaliesListView.getItems().isEmpty()){
                            clearInfoOnScreen();
                        }
                    });
                }
            }
        }
        else{
            logger.error("MWC: Trying to remove anomaly that doesn't exist");
        }
    }

    private void setConnectedLabel(String msg, Color color){
        Platform.runLater(() -> {
            connectedLabel.setText(msg);
            connectedLabel.setTextFill(color);
        });
    }

    private void setConnectedLabel(String msg){
        setConnectedLabel(msg,Color.BLACK);
    }

    public void setConnectedState(){
        chartsController.checkConnection();
        if(connectedFlag){
            this.setConnectedLabel(Connector.getAddressServerInfo(), Color.BLACK);
            Platform.runLater(() -> {
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
            });
        }
        else {
            this.setConnectedLabel("NOT CONNECTED", Color.RED);
            Platform.runLater(() -> {
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
                resetServerInfoLabels();
            });
        }
    }

    public void setChartsController(ChartsController chartsController){
        this.chartsController = chartsController;
    }

    private void resetServerInfoLabels(){
        leverValueLabel.setText("");
        anomalyLiveTimeLabel.setText("");
        BaselineWindowSizeLabel.setText("");
        shiftLabel.setText("");
        anomalyPortNrLabel.setText("");
    }

    private String formatDate(DateTime date){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
        return dtf.print(date);
    }

    public void setAvailableRoutes(){
        Platform.runLater(()->{
            monitoredRoutesComboBox.getItems().clear();
            Map<String, List<Integer>> map = ServerDatesInfo.getDates();
            if (map != null)
                monitoredRoutesComboBox.getItems().addAll(map.keySet());

        });
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
                            Thread.sleep(3000);
                            i++;
                        }
                        connectedFlag = Connector.isConnectedToTheServer();
                        setConnectedState();
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

    public void updateServerInfo(double leverValue, int anomalyLiveTime, int baselineWindowSize, AnomalyOperationProtos.SystemGeneralMessage.Shift shift, int anomalyMessagesPort){
        Platform.runLater(() -> {
            leverValueLabel.setText(String.valueOf(leverValue));
            anomalyLiveTimeLabel.setText(String.valueOf(anomalyLiveTime));
            BaselineWindowSizeLabel.setText(String.valueOf(baselineWindowSize));
            shiftLabel.setText(String.valueOf(shift));
            anomalyPortNrLabel.setText(String.valueOf(anomalyMessagesPort));
        } );
    }

    private void setupTooltips() {
        saveDefaultButton.setTooltip(new Tooltip("Save this Server Address and Server Port as default - " +
                "next time you start this application saved values will be inserted there automatically"));
        resetDefaultButton.setTooltip(new Tooltip("Load Server address and Port number saved as default"));
        chartsButton.setTooltip(new Tooltip("Go to the charts module to check local or server historical data"));
        hideServerSettingsButton.setTooltip(new Tooltip("Hides/Shows Server Settings section to make more space for other modules"));
        hideAnomaliesButton.setTooltip(new Tooltip("Hides/Shows Anomalies section to make more space for other modules"));
        leverValueLabel.setTooltip(new Tooltip("User defined number representing how much baseline can be exceeded in " +
                "addition to regularly calculated margin for a duration to be qualified as an anomaly"));
        leverValueLabelText.setTooltip(new Tooltip("User defined number representing how much baseline can be exceeded in " +
                "addition to regularly calculated margin for a duration to be qualified as an anomaly"));
        anomalyLiveTimeLabel.setTooltip(new Tooltip("Time until an anomaly will be considered as expired unless another anomaly message arrives"));
        anomalyLiveTimeLabelText.setTooltip(new Tooltip("Time until an anomaly will be considered as expired unless another anomaly message arrives"));
        BaselineWindowSizeLabel.setTooltip(new Tooltip("Time window of baseline that Server application uses to compare " +
                "current with duration of drive time when detecting anomalies"));
        BaselineWindowSizeLabelText.setTooltip(new Tooltip("Time window of baseline that Server application uses to compare " +
                "current with duration of drive time when detecting anomalies"));
        shiftLabel.setTooltip(new Tooltip("Mode which Server application currently uses - Night shift means less frequent API requests"));
        shiftLabelText.setTooltip(new Tooltip("Mode which Server application currently uses - Night shift means less frequent API requests"));
        anomalyPortNrLabel.setTooltip(new Tooltip("Port used to receive anomalies from Server application"));
        anomalyPortNrLabelText.setTooltip(new Tooltip("Port used to receive anomalies from Server application"));
        serverAddrTxtField.setTooltip(new Tooltip("IP Address of the Server application"));
        serverAddrLabel.setTooltip(new Tooltip("IP Address of the Server application"));
        serverPortTxtField.setTooltip(new Tooltip("Port used to connect Client application to Management channel of Server application"));
        serverPortLabel.setTooltip(new Tooltip("Port used to connect Client application to Management channel of Server application"));
    }

    private void setupCharts(){
        lineChart.setTitle("Selected anomaly chart");
        lineChart.setAnimated(false);
        allAnomaliesLineChart.setAnimated(false);
        //Panning works via either secondary (right) mouse or primary with ctrl held down
        ChartPanManager panner = new ChartPanManager( lineChart );
        panner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY &&
                    (mouseEvent.getButton() != MouseButton.PRIMARY ||
                            !mouseEvent.isShortcutDown()))
                                mouseEvent.consume();

        });
        panner.start();

        //Zooming works only via primary mouse button without ctrl held down
        JFXChartUtil.setupZooming( lineChart, mouseEvent -> {
            if ( mouseEvent.getButton() != MouseButton.PRIMARY ||
                    mouseEvent.isShortcutDown() )
                mouseEvent.consume();
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler( lineChart );

        ChartPanManager allAnomaliesPanner = new ChartPanManager( allAnomaliesLineChart );
        allAnomaliesPanner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY &&
                    (mouseEvent.getButton() != MouseButton.PRIMARY ||
                            !mouseEvent.isShortcutDown()))
                                mouseEvent.consume();

        });
        allAnomaliesPanner.start();

        //Zooming works only via primary mouse button without ctrl held down
        JFXChartUtil.setupZooming( allAnomaliesLineChart, mouseEvent -> {
            if ( mouseEvent.getButton() != MouseButton.PRIMARY ||
                    mouseEvent.isShortcutDown() )
                mouseEvent.consume();
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler( allAnomaliesLineChart );
    }

    @FXML
    private void initialize() throws IOException {
        systemTab.setGraphic(new Label("System info"));
        putSystemMessageOnScreen("NOT CONNECTED",Color.RED);
        systemTab.getGraphic().setStyle("-fx-text-fill: black;");
        setConnectedState();
        connectButton.setDefaultButton(true);
        hideServerSettingsButton.setText("Hide Server Settings");
        hideAnomaliesButton.setText("Hide Anomalies");
        try {
            serverAddrTxtField.setText((String) options.getPreference("Server_Address", String.class));
            serverPortTxtField.setText((String) options.getPreference("Server_Port", String.class));
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            logger.error("Options exception " + illegalPreferenceObjectExpected,illegalPreferenceObjectExpected);
        }
        setupTooltips();
        setupCharts();
        monitoredRoutesComboBox.setPromptText("Show List");
    }


    @FXML
    private void handleChartsButtonAction(ActionEvent e) {
        if (chartsController == null) {
            chartsController = new ChartsController(primaryStage, this);
            chartsController.show();
        }
        else if(chartsController.isInitialized()){
            chartsController.setScene();
        }
        else {
            chartsController.show();
        }
    }

    @FXML
    private void handleConnectAction(ActionEvent e) {
        Platform.runLater(() -> {
            connectButton.setDisable(true);
            setConnectedLabel("connecting");
        } );
        Connector.setIsFromConnecting(true);
        try {
            String address = serverAddrTxtField.getText();
            if (Pattern.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",address.trim())) {
                serverAddrTxtField.setStyle("-fx-text-box-border: black;");
            } else {
                logger.error("Wrong server address pattern");
                serverAddrTxtField.setStyle("-fx-text-box-border: red;");
                Platform.runLater(() -> connectButton.setDisable(false));
                return;
            }
            String port = serverPortTxtField.getText();
            if(!Pattern.matches("\\d+",port.trim())){
                logger.error("Wrong server port pattern");
                serverPortTxtField.setStyle("-fx-text-box-border: red;");
                Platform.runLater(() -> connectButton.setDisable(false));
                return;
            }
            else{
                serverPortTxtField.setStyle("-fx-text-box-border: black;");
            }
            Connector.connect(address.trim(), port.trim());
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        int i = 0;
                        connectedFlag = Connector.isConnectedToTheServer();
                        while(i<10 && !connectedFlag){
                            connectedFlag = Connector.isConnectedToTheServer();
                            Thread.sleep(500);
                            i++;
                        }
                        if(!connectedFlag) putSystemMessageOnScreen("Failed to connect to " + Connector.getAddressServerInfo(), Color.RED);
                        else putSystemMessageOnScreen("Connected to: " + Connector.getAddressServerInfo());
                        setConnectedState();
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
        if(connectedFlag) putSystemMessageOnScreen("Failed to disconnect from " + Connector.getAddressServerInfo(), Color.RED);
        setConnectedState();
    }

    @FXML
    private void handleAnomalyClicked(MouseEvent e) {
        System.gc();
        String selectedItem = getSelectedAnomalyId();
        if(selectedItem != null){
            lineChart.getXAxis().setAutoRanging(true);
            lineChart.getYAxis().setAutoRanging(true);
            putAnomalyInfoOnScreen(selectedItem);
            if("anomaly map".equalsIgnoreCase(tabPane.getSelectionModel().getSelectedItem().getText())) {
                //putAnomalyOnMap(selectedItem);
            }
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
        if(allAnomaliesLineChart != null && allAnomaliesLineChart.getData() != null && !allAnomaliesLineChart.getData().isEmpty()){
            allAnomaliesLineChart.getData().clear();
        }
        Label lab = (Label)tabPane.getSelectionModel().getSelectedItem().getGraphic();

        if(lab != null && lab.getText().equalsIgnoreCase("System info")){
            lab.setStyle("-fx-text-fill: black;");
        }
        else if("anomaly map".equalsIgnoreCase(tabPane.getSelectionModel().getSelectedItem().getText())){
            String a = getSelectedAnomalyId();
            if(a != null) {
                //putAnomalyOnMap(anomaliesListView.getSelectionModel().getSelectedItem());
            }
        }
        else if("anomalies summary chart".equalsIgnoreCase(tabPane.getSelectionModel().getSelectedItem().getText())){
            drawAnomaliesSummaryChart();
        }
    }
    @FXML
    private  void  handleHideAction(){
        if(hideBox.isVisible()){
            hideBox.setVisible(false);
            Platform.runLater(()->hideServerSettingsButton.setText("Show Server Settings"));
        }
        else {
            hideBox.setVisible(true);
            Platform.runLater(()->hideServerSettingsButton.setText("Hide Server Settings"));
        }
    }
    @FXML
    private void handleAnomalyPressed(KeyEvent e){
        System.gc();
        String selectedItem = getSelectedAnomalyId();
        if(selectedItem != null){
            lineChart.getXAxis().setAutoRanging(true);
            lineChart.getYAxis().setAutoRanging(true);
            putAnomalyInfoOnScreen(selectedItem);
            //putAnomalyOnMap(selectedItem);
        }
    }
    @FXML
    private void handleHideAnomaliesAction(ActionEvent e){
        if(anomaliesListView.isVisible()){
            anomaliesListView.setVisible(false);
            anomaliesGridPane.setVisible(false);
            anomaliesListLabel.setVisible(false);
            anomaliesListHBox.setVisible(false);
            Platform.runLater(()->hideAnomaliesButton.setText("Show Anomalies"));
        }
        else{
            anomaliesListView.setVisible(true);
            anomaliesGridPane.setVisible(true);
            anomaliesListLabel.setVisible(true);
            anomaliesListHBox.setVisible(true);
            Platform.runLater(()->hideAnomaliesButton.setText("Hide Anomalies"));
        }
    }
    @FXML
    private void handleHideTabsAction(ActionEvent e){
        if(tabPane.isVisible()){
            tabPane.setVisible(false);
        }
        else{
            tabPane.setVisible(true);
        }
    }
    @FXML
    private void handleResetDefaultAction(ActionEvent e){
        Platform.runLater(()->{
            try {
                serverAddrTxtField.setText((String) options.getPreference("Server_Address", String.class));
                serverPortTxtField.setText((String) options.getPreference("Server_Port", String.class));
            } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
                logger.error("Options exception " + illegalPreferenceObjectExpected,illegalPreferenceObjectExpected);
            }
        });
    }

}


