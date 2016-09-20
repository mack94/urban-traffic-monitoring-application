package pl.edu.agh.pp.charts.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Dawid on 2016-06-06.
 */
public class RoutesLoader {

    private final Logger logger = (Logger) LoggerFactory.getLogger(RoutesLoader.class);

    private static final String routesFileName = "/Routes.txt";
    private BufferedReader br;
    private String line;

    public RoutesLoader() {
    }


    public void loadRoutes(Input input) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(routesFileName)));
//        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.class.getResourceAsStream("Routes.txt"))));
        line = br.readLine();
        while(line!=null) {
            String buffer[] = line.split("-");
            input.addRoute(buffer[0].trim(), buffer[1].trim(), buffer[2].trim());
            line = br.readLine();
        }
    }

}
