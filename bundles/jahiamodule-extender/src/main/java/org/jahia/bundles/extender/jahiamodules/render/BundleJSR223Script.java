package org.jahia.bundles.extender.jahiamodules.render;

import org.apache.commons.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;

/**
 */
public class BundleJSR223Script implements Script {

    private static Logger logger = LoggerFactory.getLogger(BundleJSR223Script.class);

    private BundleView view;

    public BundleJSR223Script(BundleView view) {
        this.view = view;
    }

    @Override
    public String execute(Resource resource, RenderContext context) throws RenderException {
        ScriptEngine scriptEngine = null;
        try {
            scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(view.getFileExtension());
        } catch (ScriptException e) {
            logger.error("Error retrieving script engine for extension " + view.getFileExtension(), e);
        }
        if (scriptEngine != null) {
            ScriptContext scriptContext = new SimpleScriptContext();
            final Bindings bindings = new SimpleBindings();
            Enumeration attrNamesEnum = context.getRequest().getAttributeNames();
            while (attrNamesEnum.hasMoreElements()) {
                String currentAttributeName = (String) attrNamesEnum.nextElement();
                if (!"".equals(currentAttributeName)) {
                    bindings.put(currentAttributeName, context.getRequest().getAttribute(currentAttributeName));
                }
            }
            URL scriptURL = view.getBundle().getResource(view.getPath());
            if (scriptURL != null) {
                Reader scriptContent = null;
                try {
                    InputStream scriptInputStream = scriptURL.openStream();
                    scriptContent = new InputStreamReader(scriptInputStream);
                    scriptContext.setWriter(new StringWriter());
                    scriptContext.setErrorWriter(new StringWriter());
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                    scriptContext.setBindings(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
                    scriptEngine.eval(scriptContent, scriptContext);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    return writer.toString().trim();
                } catch (ScriptException e) {
                    throw new RenderException("Error while executing script " + view.getPath() + " from bundle " + view.getBundle().toString(), e);
                } catch (IOException e) {
                    throw new RenderException("Error while executing script " + view.getPath() + " from bundle " + view.getBundle().toString(), e);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public View getView() {
        return view;
    }
}
