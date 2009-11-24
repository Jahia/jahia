package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 4:47:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleFilter extends AbstractFilter {

    public String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException, RepositoryException {
        Script script = (Script) renderContext.getRequest().getAttribute("script");

        String addFilter = script.getTemplate().getProperties().getProperty("addFilter");
//        if (addFilter != null) {
//            try {
//                Class c = Class.forName(addFilter)
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
        return chain.doFilter(renderContext, resource, output);
    }
}
