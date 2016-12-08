package pl.edu.agh.pp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.utils.Record;

import java.util.List;

/**
 * Created by Maciej on 19.07.2016.
 * 21:22
 * Project: detector.
 */
public class XYLineChart_AWT extends ApplicationFrame {

    private final Logger logger = (Logger) LoggerFactory.getLogger(XYLineChart_AWT.class);

    private XYSeriesCollection dataset;

    public XYLineChart_AWT(String applicationTitle, String chartTitle, double[] values, List<Record> anomalousRecords) {
        super(applicationTitle);

        setSampleDataset(values, anomalousRecords);

        JFreeChart chart = ChartFactory.createXYLineChart(
                chartTitle,
                "-", "-",
                getSampleDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        setChartParameters(chart);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
    }

    private void setChartParameters(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, false);
        plot.setRenderer(renderer);
        plot.getDomainAxis().setLabel("Time of day[h]");
        plot.getRangeAxis().setLabel("Duration in traffic[s]");
    }

    public void setSampleDataset(double[] values, List<Record> anomalousRecords) {
        dataset = new XYSeriesCollection();
        XYSeries baseline = new XYSeries("baseline");
        XYSeries anomalies = new XYSeries("anomalies");

        double idx = 0;
        for (double value : values) {
            baseline.add(idx / 60, value);
            idx++;
        }

        for (Record record : anomalousRecords) {
            anomalies.add(((double) record.getTimeInSeconds()) / 3600, record.getDurationInTraffic());
        }
        dataset.addSeries(baseline);
        dataset.addSeries(anomalies);
    }

    public XYSeriesCollection getSampleDataset() {
        return dataset;
    }

}
