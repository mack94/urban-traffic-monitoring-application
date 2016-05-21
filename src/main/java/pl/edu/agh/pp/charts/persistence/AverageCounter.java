package pl.edu.agh.pp.charts.persistence;

/**
 * Created by Jakub Janusz on 21.05.2016.
 * 19:32
 * charts
 */
public class AverageCounter {

    private double total;
    private int amount;

    public AverageCounter() {
        this.total = 0;
        this.amount = 0;
    }

    public void addValue(double value) {
        total += value;
        amount++;
    }

    public double getAverage() {
        return total / amount;
    }

    public int getAmount() {
        return amount;
    }

}
