package org.jahia.services.render.filter;


import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.utils.StringResponseWrapper;

public class EditModeFilter extends AbstractFilter {

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!renderContext.getServletPath().endsWith("frame")) {
            StringResponseWrapper wrapper = new StringResponseWrapper(renderContext.getResponse());
            renderContext.getRequest().setAttribute("currentResource", resource);
            renderContext.getRequest().setAttribute("renderContext", renderContext);
            renderContext.getRequest().setAttribute("servletPath",renderContext.getRequest().getServletPath());
            renderContext.getRequest().setAttribute("url",new URLGenerator(renderContext, resource));
            renderContext.getRequest().getRequestDispatcher("/engines/edit.jsp").forward(renderContext.getRequest(), wrapper);
            return wrapper.getString();
        }
        return super.prepare(renderContext, resource, chain);
    }
}
