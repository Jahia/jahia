package org.jahia.modules.defaultmodule.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.WebUtils;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by kevan on 28/03/14.
 */
public class JCRRestJavaScriptLibFilter extends AbstractFilter{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(JCRRestJavaScriptLibFilter.class);

    public static final String JCR_REST_UTIL_INIT_DEBUG = "debug";
    public static final String JCR_REST_UTIL_INIT_API_BASE = "jcrRestAPIBase";
    public static final String JCR_REST_UTIL_INIT_API_VERSION = "jcrRestAPIVersion";
    public static final String JCR_REST_UTIL_INIT_API_CURRENT_LOCALE = "currentLocale";
    public static final String JCR_REST_UTIL_INIT_API_CURRENT_WORKSPACE = "currentWorkspace";
    public static final String JCR_REST_UTIL_INIT_API_CURRENT_RESOURCE_PATH = "currentRessourcePath";
    public static final String JCR_REST_UTIL_INIT_API_CURRENT_RESOURCE_IDENTIFIER = "currentRessourceIdentifier";

    private static final String JCR_REST_SCRIPT_TEMPLATE = "jcrRestUtilsInit.groovy";
    private static final String JCR_REST_JS_FILE = "JCRRestUtils.js";

    private ScriptEngineUtils scriptEngineUtils;
    private String resolvedTemplate;
    private String jcrRestAPIVersion;
    private Boolean debugEnabled;

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = previousOut;
        String context = StringUtils.isNotEmpty(renderContext.getRequest().getContextPath()) ? renderContext.getRequest().getContextPath() : "";

        // add lib
        String path = context + "/modules/assets/javascript/" + JCR_REST_JS_FILE;
        String encodedPath = URLEncoder.encode(path, "UTF-8");
        out += ("<jahia:resource type='javascript' path='" + encodedPath + "' insert='true' resource='" + JCR_REST_JS_FILE + "'/>");

        // instance JavaScript object
        String script = getResolvedTemplate();
        if (script != null) {
            String extension = StringUtils.substringAfterLast(JCR_REST_SCRIPT_TEMPLATE, ".");
            ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(extension);
            ScriptContext scriptContext = new JCRRestUtilsScriptContext();
            final Bindings bindings = scriptEngine.createBindings();

            // bindings
            bindings.put("options", getBindingMap(renderContext, resource, context));
            scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            // The following binding is necessary for Javascript, which doesn't offer a console by default.
            bindings.put("out", new PrintWriter(scriptContext.getWriter()));
            scriptEngine.eval(script, scriptContext);
            StringWriter writer = (StringWriter) scriptContext.getWriter();
            final String resultScript = writer.toString();
            if (StringUtils.isNotBlank(resultScript)) {
                out += ("<jahia:resource type='inlinejavascript' path='" + URLEncoder.encode(resultScript, "UTF-8") + "' insert='false' resource='' title='' key=''/>");
            }
        }

        return out;
    }

    private HashMap<String, Object> getBindingMap(RenderContext renderContext, Resource resource, String context) throws RepositoryException {
        HashMap<String, Object> bindingMap = new HashMap<String, Object>();
        String APIVersion = StringUtils.isNotEmpty(jcrRestAPIVersion) ? jcrRestAPIVersion : "v1";
        bindingMap.put(JCR_REST_UTIL_INIT_DEBUG, debugEnabled);
        bindingMap.put(JCR_REST_UTIL_INIT_API_VERSION, stringifyJsParam(APIVersion));
        bindingMap.put(JCR_REST_UTIL_INIT_API_BASE, stringifyJsParam(context + "/modules/api/jcr/" + APIVersion));
        bindingMap.put(JCR_REST_UTIL_INIT_API_CURRENT_WORKSPACE, stringifyJsParam(renderContext.getWorkspace()));
        bindingMap.put(JCR_REST_UTIL_INIT_API_CURRENT_LOCALE, stringifyJsParam(resource.getLocale().toString()));
        bindingMap.put(JCR_REST_UTIL_INIT_API_CURRENT_RESOURCE_IDENTIFIER, stringifyJsParam(resource.getNode().getIdentifier()));
        bindingMap.put(JCR_REST_UTIL_INIT_API_CURRENT_RESOURCE_PATH, stringifyJsParam(resource.getNode().getPath()));

        return bindingMap;
    }

    private String stringifyJsParam(String param) {
        return "'" + param + "'";
    }

    protected String getResolvedTemplate() throws IOException {
        if (resolvedTemplate == null) {
            String templatePath = "/modules/default/WEB-INF/scripts/" + JCR_REST_SCRIPT_TEMPLATE;
            resolvedTemplate = WebUtils.getResourceAsString(templatePath);
            if (resolvedTemplate == null) {
                logger.warn("Unable to lookup template at {}", JCR_REST_SCRIPT_TEMPLATE);
            }
        }
        return resolvedTemplate;
    }


    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }

    public void setJcrRestAPIVersion(String jcrRestAPIVersion) {
        this.jcrRestAPIVersion = jcrRestAPIVersion;
    }

    public void setDebugEnabled(Boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    class JCRRestUtilsScriptContext extends SimpleScriptContext {
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
