package pl.edu.agh.pp.charts.data.local;

import ch.qos.logback.classic.Logger;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.settings.Options;
import pl.edu.agh.pp.charts.settings.exceptions.IllegalPreferenceObjectExpected;

import java.util.List;

/**
 * Created by Maciej on 16.12.2015.
 * 17:05
 * Project: Communicator_Messages_2.
 */

public class HtmlBuilder {

    private static final String MAP_KEY_ATTRIBUTE_NAME = "MAPS_API_KEY"; //do not change this value
    private static String Google_Maps_Javascript_API_KEY;
    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass().toString());
    private String rootDir;

    public HtmlBuilder() throws IllegalPreferenceObjectExpected {
        this.rootDir = System.getProperty("user.dir");
        Options options = Options.getInstance();
        Google_Maps_Javascript_API_KEY = (String) options.getPreference("MAPS_API_KEY", String.class);
        System.out.println("Current api Key is: " + Google_Maps_Javascript_API_KEY);

    }

    public static void reloadApiKey() throws IllegalPreferenceObjectExpected {
        Options options = Options.getInstance();
        Google_Maps_Javascript_API_KEY = (String) options.getPreference("MAPS_API_KEY", String.class);
        System.out.println("Current api Key is: " + Google_Maps_Javascript_API_KEY);
    }

    private StringTemplate loadStringTemplate(String template) {

        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplateGroup group = new StringTemplateGroup(template, DefaultTemplateLexer.class);

        return group.getInstanceOf(template);
    }

    public String loadAnomalyMapStructure(MapRoute route) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("anomaly_map_structure");
        messageStructure.setAttribute("default", "false");
        messageStructure.setAttribute("startLat", route.getStartLat());
        messageStructure.setAttribute("startLng", route.getStartLng());
        messageStructure.setAttribute("endLat", route.getEndLat());
        messageStructure.setAttribute("endLng", route.getEndLng());
        messageStructure.setAttribute(MAP_KEY_ATTRIBUTE_NAME, Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }

    public String loadDefaultAnomalyMapStructure() {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("anomaly_map_structure");
        messageStructure.setAttribute("default", "true");
        //center of default anomaly map
        messageStructure.setAttribute("startLat", "50.07");
        messageStructure.setAttribute("startLng", "19.94");
        messageStructure.setAttribute(MAP_KEY_ATTRIBUTE_NAME, Google_Maps_Javascript_API_KEY);

        //these coords wont matter, but they have to be give a value
        messageStructure.setAttribute("endLat", "0");
        messageStructure.setAttribute("endLng", "0");

        return messageStructure.toString();
    }

    public String loadMapStructure(List<MapRoute> routes) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");
        StringBuilder routesInstructions = new StringBuilder();

        for (MapRoute mapRoute : routes) {
            routesInstructions.append(mapRoute.getRouteJavaScriptInstruction());
            routesInstructions.append("\n");
        }
        messageStructure.setAttribute("default", "false");
        messageStructure.setAttribute("routesInstructions", routesInstructions.toString());
        messageStructure.setAttribute(MAP_KEY_ATTRIBUTE_NAME, Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }

    public String loadDefaultMapStructure() {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");

        messageStructure.setAttribute("default", "true");
        messageStructure.setAttribute("routesInstructions", "");
        messageStructure.setAttribute(MAP_KEY_ATTRIBUTE_NAME, Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }
}
