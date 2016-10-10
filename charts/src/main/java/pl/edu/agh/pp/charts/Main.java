package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.controller.MainWindowController;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Main extends Application {
    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindowController mainWindowController = new MainWindowController(primaryStage);
        mainWindowController.show();

    }
}
