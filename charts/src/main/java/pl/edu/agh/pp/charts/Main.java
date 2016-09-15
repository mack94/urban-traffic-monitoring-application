package pl.edu.agh.pp.charts;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.pp.charts.adapters.ChannelReceiver;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.controller.MainWindowController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        Connector connector = new Connector();
        connector.setController(mainWindowController);
        System.out.println("Type server address: ");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();

        InetAddress server_addr = InetAddress.getByName(line);
        int server_port = 7500;
        boolean nio = true;

        ChannelReceiver client = new ChannelReceiver();
        client.start(server_addr, server_port, nio);

        mainWindowController.show();

    }
}
