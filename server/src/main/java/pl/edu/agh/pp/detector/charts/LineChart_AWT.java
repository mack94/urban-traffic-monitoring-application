package pl.edu.agh.pp.detector.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Maciej on 19.07.2016.
 * 21:22
 * Project: detector.
 */
public class LineChart_AWT extends ApplicationFrame {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LineChart_AWT.class);

    private DefaultCategoryDataset dataset;

    public LineChart_AWT(String applicationTitle, String chartTitle, double[] values) {
        super(applicationTitle);

        setSampleDataset(values);

        JFreeChart lineChart = ChartFactory.createLineChart(
                chartTitle,
                "-", "-",
                getSampleDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
    }

    public DefaultCategoryDataset getSampleDataset() {
        return dataset;
    }

    public void setSampleDataset(double[] values) {
        dataset = new DefaultCategoryDataset();

        int idx = 0;
        for (double value : values) {
            dataset.addValue(value, "traffic", String.valueOf(idx * 60));
            idx++;
        }
    }
}
