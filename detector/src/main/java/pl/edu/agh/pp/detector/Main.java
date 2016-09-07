package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import org.jgroups.JChannel;
import org.jgroups.Message;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.detectors.BasicDetector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.records.Record;
import pl.edu.agh.pp.detector.service.CommunicationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class Main {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static BasicDetector basicDetector = new BasicDetector();
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\Maciej\\Downloads\\9-16(1)\\9-16.txt");

    public static void main(String[] args) throws Exception {
        try {
//            filesLoader.processLineByLine();
//            PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());
//
//            LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(DayOfWeek.FRIDAY, 0));
//            chart.pack();
//            RefineryUtilities.centerFrameOnScreen(chart);
//            chart.setVisible(true);
//
//            System.out.println(basicDetector.isAnomaly(DayOfWeek.FRIDAY, 0, 10200, 518)); // FIXME: To not hardcoded.
//
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            InputParser inputParser = new InputParser();
////
//            System.setProperty("java.net.preferIPv4Stack" , "true");
//            System.setProperty("jgroups.bind_addr", "230.1.2.224");
            CommunicationService service = new CommunicationService();
            service.setUserName("AnomalyDetector");
            String incoming = br.readLine();
            service.joinManagementChannel();
            service.joinChannel(incoming);

//
            while (true) {
//                String incoming = br.readLine();
//                Record record = inputParser.parse(incoming);

//                if (incoming != "") {
//                    chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(record.getDayOfWeek(), record.getRouteID() - 9));
//                    chart.pack();
//                    RefineryUtilities.centerFrameOnScreen(chart);
//                    chart.setVisible(true);
//                }

//                service.sendMessage("224", (record.getRouteID() + " " + basicDetector.isAnomaly(record.getDayOfWeek(, record.getRouteID() - 9, record.getTimeInSeconds(), record.getDurationInTraffic())) );
                service.sendMessage("12", "Test message");
                Thread.sleep(10000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
