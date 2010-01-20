package org.jahia.services.render.filter;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.profiler.Profiler;

/**
 * TemplateScriptFilter
 *
 * Execute the template script associated to the current resource.
 *
 * This is a final filter, subsequent filters will not be chained.
 *
 */
public class TemplateScriptFilter extends AbstractFilter {

    private static Logger logger = Logger.getLogger(TemplateScriptFilter.class);
    
    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws RenderException {
        Profiler profiler = (Profiler) renderContext.getRequest().getAttribute("profiler");
        if (profiler != null) {
            profiler.start("render template "+resource.getResolvedTemplate());
        }

        HttpServletRequest request = renderContext.getRequest();
        Script script = (Script) request.getAttribute("script");
        renderContext.getResourcesStack().push(resource);
        String output;
        try {
            output = script.execute(resource, renderContext);
        } catch (RenderException e) {
            output = handleError(script.getTemplate().getInfo(), e, renderContext, resource);
        } finally {
            renderContext.getResourcesStack().pop();
        }
        return output;
    }
    
    private static String handleError(String template, RenderException ex, RenderContext ctx, Resource resource) throws RenderException {

        String content = null;

        String onError = SettingsBean.getInstance().lookupString("templates.modules.onError");

        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

        if ("propagate".equals(onError)) {
            throw ex;
        } else if ("hide".equals(onError)) {
            logger.warn("Error including the content of the template '" + template
                    + "'. Cause: " + cause.getMessage(), cause);
            content = "";
        } else {
            logger.warn("Error including the content of the template '" + template
                    + "'. Cause: " + cause.getMessage(), cause);
            StringBuilder out = new StringBuilder(256);
            out.append("<div class=\"page-fragment-error\">").append(getErrorMessage(ctx, resource)).append(
                    !"compact".equals(onError) ? ": " + getExceptionDetails(cause) : "").append("</div>");

            content = out.toString();
        }

        return content;
    }

    private static String getErrorMessage(RenderContext ctx, Resource resource) {
        return JahiaResourceBundle.getString(null, "templates.modules.onError", resource.getLocale(), ctx.getSite().getTemplatePackageName());
    }
    
    private static Object getExceptionDetails(Throwable ex) {
        StringWriter out = new StringWriter();
        out.append(ex.getMessage()).append("\n<!--\n");
        ex.printStackTrace(new PrintWriter(out));
        out.append("\n-->\n");

        return out.toString();
    }

}