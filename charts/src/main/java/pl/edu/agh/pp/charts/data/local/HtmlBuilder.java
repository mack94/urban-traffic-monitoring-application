package pl.edu.agh.pp.charts.data.local;

import ch.qos.logback.classic.Logger;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Maciej on 16.12.2015.
 * 17:05
 * Project: Communicator_Messages_2.
 */

public class HtmlBuilder {

    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass().toString());
    private String rootDir;

    public HtmlBuilder() {
        this.rootDir = System.getProperty("user.dir");
    }

    private StringTemplate loadStringTemplate(String template) {

        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplateGroup group = new StringTemplateGroup(template, DefaultTemplateLexer.class);

        return group.getInstanceOf(template);
    }

    public String loadAnomalyMapStructure(MapRoute route) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("anomaly_map_structure");
        messageStructure.setAttribute("startLat", route.getStartLat());
        messageStructure.setAttribute("startLng", route.getStartLng());
        messageStructure.setAttribute("endLat", route.getEndLat());
        messageStructure.setAttribute("endLng", route.getEndLng());

        return messageStructure.toString();
    }

    public String loadMapStructure(List<MapRoute> routes) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");
        StringBuilder routesInstructions = new StringBuilder();

        for(MapRoute mapRoute: routes) {
            routesInstructions.append(mapRoute.getRouteJavaScriptInstruction());
            routesInstructions.append("\n");
        }
        messageStructure.setAttribute("routesInstructions", routesInstructions.toString());

        return messageStructure.toString();
    }
}
