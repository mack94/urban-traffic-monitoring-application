package pl.edu.agh.pp;

import pl.edu.agh.pp.cron.CronManager;
import pl.edu.agh.pp.detector.adapters.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Jakub Janusz on 07.09.2016.
 * 20:18
 * server
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        InetAddress bind_addr = null; // FIXME
        try {
            bind_addr = InetAddress.getByName("192.168.1.12");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int port = 7500;
        boolean nio = true;

        System.out.println("Running server in 5 sec.");
        Thread.sleep(5000);
        Server server = new Server();
        try {
            server.start(bind_addr, port, nio);
            System.out.println("Server already running.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(15000);
        new CronManager(server).doSomething();
    }

}
