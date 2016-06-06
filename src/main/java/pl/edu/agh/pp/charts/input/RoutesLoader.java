package pl.edu.agh.pp.charts.input;

import java.io.*;

/**
 * Created by Dawid on 2016-06-06.
 */
public class RoutesLoader {
    private static final String routesFileName = "Routes.txt";
    private File file;
    private BufferedReader br;
    private String line;


    public void loadRoutes(Input input) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(routesFileName)));
        line = br.readLine();
        while(line!=null) {
            String buffer[] = line.split("-");
            input.addRoute(buffer[0].trim(), buffer[1].trim(), buffer[2].trim());
            line = br.readLine();
        }
    }

}
