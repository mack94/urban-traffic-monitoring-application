package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.service.CommunicationService;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Main extends Application {
    public static void main(String... args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindowController mainWindowController = new MainWindowController(primaryStage);
        mainWindowController.show();

        // JGroups asynchronous listener
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("jgroups.client_bind_addr", "192.168.1.10");
        CommunicationService service = new CommunicationService();
        service.setUserName("Charts");
        service.joinManagementChannel();
        service.joinChannel("192.168.1.10");

    }
}
