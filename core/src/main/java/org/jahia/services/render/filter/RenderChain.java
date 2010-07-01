package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    final Map<String, Object> oldPropertiesMap = new HashMap<String, Object>();

    /**
     * Initializes an instance of this class.
     */
    public RenderChain() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param filters a list of filters to be used in the chain
     */
    public RenderChain(RenderFilter... filters) {
        this();
        for (RenderFilter renderFilter : filters) {
            addFilter(renderFilter);
        }
    }

    /**
     * Add one filter the chain.
     *
     * @param filter The filter to add
     */
    public void addFilter(RenderFilter filter) {
        this.filters.add(filter);
        Collections.sort(this.filters);
    }

    /**
     * Add multiple filters to the chain.
     *
     * @param filters The filters to add
     */
    public void addFilters(Collection<RenderFilter> filters) {
        this.filters.addAll(filters);
        Collections.sort(this.filters);
    }

    /**
     * Continue the execution of the render chain. Go to the next filter if one is available.
     *
     * If no other filter is available, throws an IOException
     * @param renderContext The render context
     * @param resource The current resource to display
     * @return Output from the next filter
     * @throws RenderFilterException in case of a rendering errors
     */
    public String doFilter(RenderContext renderContext, Resource resource) throws RenderFilterException {
        String out = null;
        int index=0;

        try {
            for (; index<filters.size() && out == null && renderContext.getRedirect() == null; index++) {
                RenderFilter filter = filters.get(index);
                if (filter.areConditionsMatched(renderContext, resource)) {
                    out = filter.prepare(renderContext, resource, this);
                }
            }
            for (; index>0 && renderContext.getRedirect() == null; index--) {
                RenderFilter filter = filters.get(index-1);
                if (filter.areConditionsMatched(renderContext, resource)) {
                    out = filter.execute(out, renderContext, resource, this);
                }
            }
        } catch (Exception e) {
            throw new RenderFilterException(e);
        } finally {
            popAttributes(renderContext.getRequest());
        }

        return out;
    }

    public void pushAttribute(HttpServletRequest request, String key, Object value) {
        oldPropertiesMap.put(key, request.getAttribute(key));
        request.setAttribute(key, value);
    }

    private void popAttributes(HttpServletRequest request) {
        for (Map.Entry<String,Object> entry : oldPropertiesMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public Object getPreviousValue(String key) {
        return oldPropertiesMap.get(key);
    }
}
