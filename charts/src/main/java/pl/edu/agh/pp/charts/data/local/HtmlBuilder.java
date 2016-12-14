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

    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass().toString());
    private String rootDir;
    private String Google_Maps_Javascript_API_KEY;

    public HtmlBuilder() throws IllegalPreferenceObjectExpected {
        this.rootDir = System.getProperty("user.dir");
        Options options = Options.getInstance();
        this.Google_Maps_Javascript_API_KEY = (String) options.getPreference("MAPS_API_KEY", String.class);
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
        messageStructure.setAttribute("MAPS_API_KEY", Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }

    public String loadDefaultAnomalyMapStructure() {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("anomaly_map_structure");
        messageStructure.setAttribute("default", "true");
        //center of default anomaly map
        messageStructure.setAttribute("startLat", "50.07");
        messageStructure.setAttribute("startLng", "19.94");
        messageStructure.setAttribute("MAPS_API_KEY", Google_Maps_Javascript_API_KEY);

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
        messageStructure.setAttribute("MAPS_API_KEY", Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }

    public String loadDefaultMapStructure() {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");

        messageStructure.setAttribute("default", "true");
        messageStructure.setAttribute("routesInstructions", "");
        messageStructure.setAttribute("MAPS_API_KEY", Google_Maps_Javascript_API_KEY);

        return messageStructure.toString();
    }
}
