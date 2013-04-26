package nl.hsac.fitnesse.fixture.util;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Helper to facilitate our FreeMarker usage
 */
public class FreeMarkerHelper {
    /**
     * @param name name of template to retrieve
     * @return template with specified name
     */
    public Template getTemplate(Configuration conf, String name) {
        Template result = null;
        try {
            result = conf.getTemplate(name, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * @param t template to apply
     * @param model model to supply to template
     * @return string containing template output
     */
    public String processTemplate(Template t, Object model) {
        StringWriter sw = new StringWriter();
        try {
            t.process(model, sw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String result = sw.toString();
        return result;
    }
    

}
