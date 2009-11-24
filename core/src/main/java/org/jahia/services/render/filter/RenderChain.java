package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 12:33:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class RenderChain {
    private List<RenderFilter> filters = new ArrayList<RenderFilter>();
    private int index = 0;

    public void addFilter(RenderFilter filter) {
        this.filters.add(filter);
    }

    public void addFilters(Collection<RenderFilter> filters) {
        this.filters.addAll(filters);
    }

    public String doFilter(RenderContext renderContext, Resource resource, String output) throws IOException, RepositoryException {
        if (filters.size() >= index) {
            RenderFilter f = filters.get(index++);
            return f.doFilter(renderContext, resource, output, this);
        } else {
            return output;
        }
    }
}
