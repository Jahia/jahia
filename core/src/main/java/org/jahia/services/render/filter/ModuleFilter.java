package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * ModuleFilter
 *
 * Composite filter containing all filters specifics to the module and/or the template to display.
 *
 */
public class ModuleFilter extends AbstractFilter {

    public String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
        Script script = (Script) renderContext.getRequest().getAttribute("script");

        String addFilter = script.getTemplate().getProperties().getProperty("addFilter");
//        if (addFilter != null) {
//            try {
//                Class c = Class.forName(addFilter)
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
        return chain.doFilter(renderContext, resource);
    }
}
