package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.controller.ChartsController;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.data.server.AnomalyManager;
import pl.edu.agh.pp.charts.settings.IOptions;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Main extends Application {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        try {
            // Initialize options
            IOptions options = Options.getInstance();
            options.initialize();

            // Initialize routes file
            File file = new File("./routes.json");
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.close();

        } catch (BackingStoreException e) {
            logger.error("Main (Charts): BackingStoreException while Options initialization: " + e);
        } catch (IOException e) {
            logger.error("Main (Charts): IO Exception while Options initialization: " + e);
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: Na wczesnym etapie, powinnismy miec juz np. zaladowane/przygotowane trasy do routes.json (z SystemGeneralMessage)
        primaryStage.setWidth(1500);
        primaryStage.setHeight(900);
        primaryStage.setMaximized(true);
        MainWindowController mainWindowController = new MainWindowController(primaryStage);
        AnomalyManager.getInstance().setController(mainWindowController);
        Connector.setMainWindowController(mainWindowController);
        mainWindowController.setChartsController(new ChartsController(primaryStage, mainWindowController));
        mainWindowController.show();
    }
}
