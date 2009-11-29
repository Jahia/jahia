package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Interface that defines a filter usable in the {@link RenderChain}.
 *
 * Each filter can either call the next filter and transform the output, or generate its own output. It can execute
 * operations before/after calling the next filter.
 *
 * Date: Nov 24, 2009
 * Time: 12:08:45 PM
 */
public interface RenderFilter extends RenderServiceAware {
    
    /**
     * Execute filtering on output. Return the final filtered output.
     *
     * @param renderContext Current RenderContext
     * @param resource Resource being displayed
     * @param chain RenderChain to use for chaining to next filter
     * @return Filtered output
     * @throws RenderFilterException in case of rendering errors
     */
    String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws RenderFilterException;

}
