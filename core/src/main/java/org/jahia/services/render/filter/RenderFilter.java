package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 12:08:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RenderFilter {

    void setService(RenderService service);

    String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException,  RepositoryException;

}
