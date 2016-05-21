package main.java.persistence;

/**
 * Created by Jakub Janusz on 21.05.2016.
 * 19:32
 * charts
 */
public class AverageCounter {

    private int total;
    private int amount;

    public AverageCounter() {
        this.total = 0;
        this.amount = 0;
    }

    public void addValue(int value) {
        total += value;
        amount++;
    }

    public double getAverage() {
        return (double)(total / amount);
    }

}
