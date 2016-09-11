package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.adapters.ChannelReceiver;
import pl.edu.agh.pp.charts.controller.MainWindowController;

import java.net.InetAddress;

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

        InetAddress server_addr = InetAddress.getByName("192.168.1.12"); // FIXME: Make me not to be hardcoded.
        int server_port = 7500;
        boolean nio = true;

        ChannelReceiver client = new ChannelReceiver();
        client.start(server_addr, server_port, nio);
    }
}
