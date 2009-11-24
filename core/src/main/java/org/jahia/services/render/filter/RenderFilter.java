package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * RenderFilter
 *
 * Interface that defines a filter usable in the RenderChain.
 *
 * Each filter can either call the next filter and transform the output, or generate its own output. It can execute
 * operations before/after calling the next filter.
 *
 * Date: Nov 24, 2009
 * Time: 12:08:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RenderFilter {

    /**
     * Set the render service - called by the service itself when the filter is registered.
     * @param service
     */
    void setService(RenderService service);

    /**
     * Execute filtering on output. Return the final filtered output.
     *
     * @param renderContext Current RenderContext
     * @param resource Resource being displayed
     * @param chain RenderChain to use for chaining to next filter
     * @return Filtered output
     * @throws IOException
     * @throws RepositoryException
     */
    String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException,  RepositoryException;

}
