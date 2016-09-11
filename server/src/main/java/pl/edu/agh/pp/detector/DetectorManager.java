package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.detector.adapters.ChannelReceiver;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.records.Record;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class DetectorManager {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static Detector detector;
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\Maciej\\Downloads\\logs_16-09-04_Sun\\TrafficLog_1_8___Sun_16-09-04.log");
//    private static ChannelReceiver client = new ChannelReceiver();
    private Server server;

    public DetectorManager(Server server) {
        try {
            filesLoader.processLineByLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detector = polynomialPatternBuilder;
        PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());
        this.server = server;
//        System.out.println("Connecting to the server in 5 seconds.");
//        try {
//            Thread.sleep(5000);
//            client.start(null, 7500, true); // FIXME
//            System.out.println("Connected to the server.");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void doSomething(String logEntry) {
        try {
            LineChart_AWT chart;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            InputParser inputParser = new InputParser();

//            while (true) {
//                String incoming = br.readLine();
            Record record = inputParser.parse(logEntry);

            if (logEntry != "") {
                chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(record.getDayOfWeek(), record.getRouteID() - 1));
                chart.pack();
                RefineryUtilities.centerFrameOnScreen(chart);
                chart.setVisible(true);
            }

            boolean isAnomaly = detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic());
            System.out.println(isAnomaly);
            if (isAnomaly) {
                server.send(ByteBuffer.wrap(String.valueOf(isAnomaly).getBytes()));
            }

            Thread.sleep(100);
//            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
