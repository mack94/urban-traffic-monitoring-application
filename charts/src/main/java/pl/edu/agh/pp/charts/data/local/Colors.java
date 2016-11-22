package pl.edu.agh.pp.charts.data.local;

import javafx.scene.paint.Color;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-22.
 */
public class Colors {
    private static final Color[] KELLY_COLORS = {
            Color.web("0x73B9FF"),    // NOT A KELLY COLOR, google blue route color
            //Color.web("0xFFB300"),    // Vivid Yellow
            Color.web("0x803E75"),    // Strong Purple
            Color.web("0xFF6800"),    // Vivid Orange
            Color.web("0xA6BDD7"),    // Very Light Blue
            //Color.web("0xC10020"),    // Vivid Red
            Color.web("0xCEA262"),    // Grayish Yellow
            Color.web("0x817066"),    // Medium Gray
    };

    private static int counter = -1;

    public static Color getNextColor() {
        counter++;
        if(counter >= KELLY_COLORS.length)
            counter = 0;
        return KELLY_COLORS[counter];
    }
}
