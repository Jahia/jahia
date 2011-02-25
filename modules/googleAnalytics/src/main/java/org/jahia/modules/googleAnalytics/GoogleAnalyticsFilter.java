package org.jahia.modules.googleAnalytics;

import net.htmlparser.jericho.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.*;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 2/25/11
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAnalyticsFilter extends AbstractFilter{

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GoogleAnalyticsFilter.class);

    private ScriptEngineUtils scriptEngineUtils;
    private String template;
    private String scriptMap;

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Source source = new Source(previousOut);
        OutputDocument outputDocument = new OutputDocument(source);
        if (renderContext.getSite().hasProperty("webPropertyID") && !renderContext.getSite().getProperty("webPropertyID").getString().equals("")) {
            List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
            for (Element element : bodyElementList) {
                final EndTag bodyEndTag = element.getEndTag();
                String extension = StringUtils.substringAfterLast(template, ".");
                ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(extension);
                ScriptContext scriptContext = new GoogleScriptContext();
                final Bindings bindings = scriptEngine.createBindings();
                bindings.put("webPropertyID", renderContext.getSite().getProperty("webPropertyID").getString());
                bindings.put("resource", resource);
                bindings.put("gaMap",renderContext.getRequest().getAttribute("gaMap"));
                scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                String resolvedTemplate = null;
                try {
                resolvedTemplate = FileUtils.readFileToString(new File(

                            JahiaContextLoaderListener.getServletContext().getRealPath(template)));
                } catch (java.io.FileNotFoundException e) {
                    logger.warn("GoogleAnalyticsFilter : Script " + template + "is not found");
                }
                if (resolvedTemplate != null) {
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    scriptEngine.eval(resolvedTemplate, scriptContext);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    final String googleAnalyticsScript = writer.toString();
                    if (StringUtils.isNotBlank(googleAnalyticsScript)) {
                        outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1,
                                "\n" + AggregateCacheFilter.removeEsiTags(googleAnalyticsScript) + "\n<");
                    }
                    break; // avoid to loop if for any reasons multiple body in the page
                }
            }
        }
        return outputDocument.toString().trim();
    }
    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }
    public void setTemplate(String template) {
        this.template = template;
    }

    public void setScriptMap(String scriptMap) {
        this.scriptMap = scriptMap;
    }

    class GoogleScriptContext extends SimpleScriptContext {
        private Writer writer = null;

        /**
         * {@inheritDoc}
         */
        @Override
        public Writer getWriter() {
            if (writer == null) {
                writer = new StringWriter();
            }
            return writer;
        }
    }
}
