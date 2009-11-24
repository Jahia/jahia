package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * RenderChain.
 *
 * Main pipeline that generates output for rendering.
 *
 * Date: Nov 24, 2009
 * Time: 12:33:52 PM
 */
public class RenderChain {
    private List<RenderFilter> filters = new ArrayList<RenderFilter>();
    private int index = 0;

    /**
     * Add one filter the chain.
     *
     * @param filter The filter to add
     */
    public void addFilter(RenderFilter filter) {
        this.filters.add(filter);
    }

    /**
     * Add multiple filters to the chain.
     *
     * @param filters The filters to add
     */
    public void addFilters(Collection<RenderFilter> filters) {
        this.filters.addAll(filters);
    }

    /**
     * Continue the execution of the render chain. Go to the next filter if one is available.
     *
     * If no other filter is available, throws an IOException
     * @param renderContext The render context
     * @param resource The current resource to display
     * @return Output from the next filter
     * @throws IOException
     * @throws RepositoryException
     */
    public String doFilter(RenderContext renderContext, Resource resource) throws IOException, RepositoryException {
        if (filters.size() >= index) {
            RenderFilter f = filters.get(index++);

            return f.doFilter(renderContext, resource, this);
        } else {
            throw new IOException("No content");
        }
    }
}
