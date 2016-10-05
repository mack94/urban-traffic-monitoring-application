package pl.edu.agh.pp.charts.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.input.Input;

import java.io.IOException;

/**
 * Created by Dawid on 2016-09-05.
 */
public class MainWindowController {
    private Stage primaryStage = null;
    private Input input;
    private ChartsController chartsController = null;
    private Image wajchaON = new Image("LeverON.png");
    private Image wajchaOFF = new Image("LeverOFF.png");
    private boolean wajchaFlag;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Button chartsButton;
    @FXML
    private TextFlow anomaliesTextFlow;
    @FXML
    private ImageView lever;
    @FXML
    private ImageView click;
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
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(Main.class.getResource("/chart.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void putAnomalyMessageonScreen(int id, String message, DateTime dateTime, int duration, Color color) {
        Text text1 = new Text(id + ": " + dateTime.toString() + "; duration = " + duration + "; " + message + "\n");
        text1.setFill(color);
        text1.setFont(Font.font("Helvetica", FontPosture.REGULAR, 16));

        Platform.runLater(() -> anomaliesTextFlow.getChildren().add(0, text1));

    }

    @FXML
    private void initialize() throws IOException {
        anomaliesTextFlow.setTextAlignment(TextAlignment.CENTER);
        anomaliesTextFlow.setMaxHeight(150);

        lever.setImage(wajchaOFF);
        click.setImage(new Image("/clickit.png"));
        wajchaFlag = false;
        putAnomalyMessageonScreen(1, "Waiting for anomalies.", DateTime.now(), 0, Color.CRIMSON);
    }

    @FXML
    private void handleChartsButtonAction(ActionEvent e) {
        if (chartsController == null) {
            chartsController = new ChartsController(primaryStage, this);
        }
        chartsController.show();
    }

    @FXML
    private void handleTestButtonAction(ActionEvent e) {
        putAnomalyMessageonScreen(666, "Test anomaly", DateTime.now(), 0, Color.PINK);
    }

    @FXML
    private void handleWajcha(MouseEvent e) {
        if (wajchaFlag) {
            lever.setImage(wajchaOFF);
            wajchaFlag = false;
            Connector.onWajcha(false);
        } else {
            lever.setImage(wajchaON);
            wajchaFlag = true;
            Connector.onWajcha(true);
        }
    }
}


