package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.input.AnomalyManager;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Main extends Application {
    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setWidth(850);
        primaryStage.setHeight(700);
        MainWindowController mainWindowController = new MainWindowController(primaryStage);
        AnomalyManager.getInstance().setController(mainWindowController);
        mainWindowController.show();
    }
}
