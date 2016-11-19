package pl.edu.agh.pp.charts.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.edu.agh.pp.charts.Main;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.data.local.HtmlBuilder;
import pl.edu.agh.pp.charts.data.local.MapRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Krzysztof on 2016-11-18.
 */
public class MapController {
    private static final String MAP_STAGE_TITLE = "Urban traffic monitoring - map";
    private Stage primaryStage = null;
    private MainWindowController parent;
    private Scene scene = null;
    private HtmlBuilder htmlBuilder;
    private WebEngine webEngine;
    private boolean initialized = false;

    @FXML
    private WebView mapWebView;

    public MapController(Stage primaryStage, MainWindowController parent) {
        this.primaryStage = primaryStage;
        this.parent = parent;
    }

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/Map.fxml"));
            loader.setController(this);
            BorderPane rootLayout = loader.load();

            primaryStage.setTitle(MAP_STAGE_TITLE);
            scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            initialized = true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() throws IOException {
        setMapUp();
    }

    private void setMapUp() {
        htmlBuilder = new HtmlBuilder();
        webEngine = mapWebView.getEngine();
        MapRoute defaultRoute1 = new MapRoute("50.07", "19.94", "50.079", "19.94");
        MapRoute defaultRoute2 = new MapRoute("50.065", "19.92", "50.073", "19.95");
        List<MapRoute> routes = new ArrayList<>();
        routes.add(defaultRoute1);
        routes.add(defaultRoute2);
        webEngine.loadContent(htmlBuilder.loadMapStructure(routes));
    }

    boolean isInitialized(){
        return initialized;
    }

    public void setScene(){
        primaryStage.setScene(scene);
        primaryStage.setTitle(MAP_STAGE_TITLE);
    }

    @FXML
    private void handleBackButtonAction(ActionEvent e) {
        parent.setScene();
    }
}
