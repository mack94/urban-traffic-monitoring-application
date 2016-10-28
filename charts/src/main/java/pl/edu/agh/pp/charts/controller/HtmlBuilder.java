package pl.edu.agh.pp.charts.controller;

import ch.qos.logback.classic.Logger;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.slf4j.LoggerFactory;

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

        StringTemplateGroup group = new StringTemplateGroup(template, rootDir + "/src/main/resources", DefaultTemplateLexer.class);

        return group.getInstanceOf(template);
    }

    public String loadMapStructure(String anomalyLat, String anomalyLng) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");
        messageStructure.setAttribute("anomalyLat", anomalyLat);
        messageStructure.setAttribute("anomalyLng", anomalyLng);

        return messageStructure.toString();
    }
}
