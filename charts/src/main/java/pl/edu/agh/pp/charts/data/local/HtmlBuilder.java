package pl.edu.agh.pp.charts.data.local;

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

        StringTemplateGroup group = new StringTemplateGroup(template, DefaultTemplateLexer.class);

        return group.getInstanceOf(template);
    }

    public String loadMapStructure(String startLat, String startLng, String endLat, String endLng) {
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());

        StringTemplate messageStructure = loadStringTemplate("map_structure");
        messageStructure.setAttribute("startLat", startLat);
        messageStructure.setAttribute("startLng", startLng);
        messageStructure.setAttribute("endLat", endLat);
        messageStructure.setAttribute("endLng", endLng);

        return messageStructure.toString();
    }
}
