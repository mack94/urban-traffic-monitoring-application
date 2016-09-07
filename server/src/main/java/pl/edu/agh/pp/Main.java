package pl.edu.agh.pp;

import pl.edu.agh.pp.cron.CronManager;

/**
 * Created by Jakub Janusz on 07.09.2016.
 * 20:18
 * server
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        new CronManager().doSomething();
    }

}
