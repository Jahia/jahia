package org.jahia.services.render.scripting;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.services.render.*;

import javax.script.*;
import java.io.*;
import java.util.Enumeration;

/**
 * JSR 223 ScriptEngine dispatcher.
 *
 * @author loom
 *         Date: Jan 15, 2010
 *         Time: 11:14:06 AM
 */
public class JSR223Script implements Script {

    private static final Logger logger = Logger.getLogger(JSR223Script.class);

    private Template template;

    /**
     * Builds the script object
     *
     */
    public JSR223Script(Template template) {
        this.template = template;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     *
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByExtension(template.getFileExtension());
        if (scriptEngine != null) {
            ScriptContext scriptContext = scriptEngine.getContext();
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            Enumeration attrNamesEnum = context.getRequest().getAttributeNames();
            while (attrNamesEnum.hasMoreElements()) {
                String currentAttributeName = (String) attrNamesEnum.nextElement();
                if (!"".equals(currentAttributeName)) {
                    bindings.put(currentAttributeName, context.getRequest().getAttribute(currentAttributeName));
                }
            }
            InputStream scriptInputStream = Jahia.getStaticServletConfig().getServletContext().getResourceAsStream(template.getPath());
            if (scriptInputStream != null) {
                Reader scriptContent = null;
                try {
                    scriptContent = new InputStreamReader(scriptInputStream);
                    scriptContext.setWriter(new StringWriter());
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    Object result = scriptEngine.eval(scriptContent, bindings);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    return writer.toString();
                } catch (ScriptException e) {
                    throw new RenderException(e.getMessage(), e);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Provides access to the template associated with this script
     *
     * @return the Template instance that will be executed
     */
    public Template getTemplate() {
        return template;
    }

}
