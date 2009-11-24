package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TemplateScriptFilter extends AbstractFilter {

    public String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException, RepositoryException {
        HttpServletRequest request = renderContext.getRequest();
        Script script = (Script) request.getAttribute("script");
        renderContext.getResourcesStack().push(resource);
        output = script.execute();
        renderContext.getResourcesStack().pop();
        return output;
    }
}