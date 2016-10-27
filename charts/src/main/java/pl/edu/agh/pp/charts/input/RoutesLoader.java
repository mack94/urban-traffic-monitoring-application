package pl.edu.agh.pp.charts.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dawid on 2016-06-06.
 */
public class RoutesLoader {

    private static final String routesFileName = "/Routes.txt";
    private final static Logger logger = (Logger) LoggerFactory.getLogger(RoutesLoader.class);
    private BufferedReader br;
    private String line;
    private static Map<String, Route> routes;

    public RoutesLoader() {
    }


    public void loadRoutes(Input input) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(routesFileName)));
//        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.class.getResourceAsStream("Routes.txt"))));
        line = br.readLine();
        while (line != null) {
            String buffer[] = line.split("-");
            input.addRoute(buffer[0].trim(), buffer[1].trim(), buffer[2].trim());
            line = br.readLine();
        }
    }
    private static void loadRoutes() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(routesFileName)));
            String line = br.readLine();
            while (line != null) {
                String buffer[] = line.split("-");
                routes.put(buffer[0].trim(), new Route(buffer[0].trim(), buffer[1].trim(), buffer[2].trim()));
                line = br.readLine();
            }
        } catch (Exception e){
            logger.error("Routes loader exception");
            e.printStackTrace();
        }

    }

    public static String getRoute(String routeId){
        if(routes == null) {
            routes = new HashMap<>();
            loadRoutes();
        }
        String route = null;
        if(routes.get(routeId) != null) {
            route = routes.get(routeId).getOrigin() + " - " + routes.get(routeId).getDestination();
        }
        return route;
    }
}
