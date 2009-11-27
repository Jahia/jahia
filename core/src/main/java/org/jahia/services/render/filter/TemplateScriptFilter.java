package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;
import org.slf4j.profiler.Profiler;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * TemplateScriptFilter
 *
 * Execute the template script associated to the current resource.
 *
 * This is a final filter, subsequent filters will not be chained.
 *
 */
public class TemplateScriptFilter extends AbstractFilter {

    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
        Profiler profiler = (Profiler) renderContext.getRequest().getAttribute("profiler");
        if (profiler != null) {
            profiler.start("render template "+resource.getResolvedTemplate());
        }

        HttpServletRequest request = renderContext.getRequest();
        Script script = (Script) request.getAttribute("script");
        renderContext.getResourcesStack().push(resource);
        String output;
        try {
            output = script.execute();
        } finally {
            renderContext.getResourcesStack().pop();
        }
        return output;
    }
}