package pl.edu.agh.pp.detector.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Created by Maciej on 19.07.2016.
 * 21:22
 * Project: detector.
 */
public class LineChart_AWT extends ApplicationFrame {

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

    public void setSampleDataset(double[] values) {
        dataset = new DefaultCategoryDataset();

        int idx = 0;
        for (double value : values) {
            dataset.addValue(value, "traffic", String.valueOf(idx*60));
            //System.out.println(idx*60);
            idx++;
        }
    }

    public DefaultCategoryDataset getSampleDataset() {
        return dataset;
    }
}
