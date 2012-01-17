/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import org.slf4j.Logger;
import org.apache.commons.collections.list.UnmodifiableList;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Main pipeline that generates output for rendering.
 *
 * Date: Nov 24, 2009
 * Time: 12:33:52 PM
 */
public class RenderChain {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RenderChain.class);

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
     * @param filters to be used
     */
    public RenderChain(Collection<RenderFilter> filters) {
        super();
        addFilters(filters);
    }

    /**
     * Initializes an instance of this class.
     * @param filters1 to be used
     * @param filters2 to be used (additionally to filters1)
     */
    public RenderChain(Collection<RenderFilter> filters1, Collection<RenderFilter> filters2) {
        super();
        this.filters.addAll(filters1);
        this.filters.addAll(filters2);
        Collections.sort(this.filters);
    }

    /**
     * Initializes an instance of this class.
     * @param filters a list of filters to be used in the chain
     */
    public RenderChain(RenderFilter... filters) {
        this();
        for (RenderFilter renderFilter : filters) {
            this.filters.add(renderFilter);
        }
        Collections.sort(this.filters);
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

        if (logger.isTraceEnabled()) {
            logger.trace("Configured filters:");
            for (RenderFilter filter : filters) {
                logger.trace("  " + filter.getClass().getName());
            }
        }

        String nodePath = "empty resource";
        if(resource!=null)
        nodePath = resource.getNode().getPath();
        try {
            for (; index<filters.size() && out == null && renderContext.getRedirect() == null; index++) {
                RenderFilter filter = filters.get(index);
                if (filter.areConditionsMatched(renderContext, resource)) {
                	long timer = System.currentTimeMillis();
                    out = filter.prepare(renderContext, resource, this);
                    if (logger.isDebugEnabled()) { 
			logger.debug("{}: prepare filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
            for (; index>0 && renderContext.getRedirect() == null; index--) {
                RenderFilter filter = filters.get(index-1);
                if (filter.areConditionsMatched(renderContext, resource)) {
                	long timer = System.currentTimeMillis();
                    out = filter.execute(out, renderContext, resource, this);
                    if (logger.isDebugEnabled()) { 
                    	logger.debug("{}: execute filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while rendering the resource: " + resource);
            for (; index>0 && renderContext.getRedirect() == null; index--) {
                RenderFilter filter = filters.get(index-1);
                if (filter.areConditionsMatched(renderContext, resource)) {
                	long timer = System.currentTimeMillis();
                    filter.handleError(renderContext, resource, this, e);
                    if (logger.isDebugEnabled()) { 
                    	logger.debug("{}: handling error for filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
            throw new RenderFilterException(e);
        } finally {
            for (; index<filters.size(); index++) {
                try {
                    RenderFilter filter = filters.get(index);
                    if (filter.areConditionsMatched(renderContext, resource)) {
                    	long timer = System.currentTimeMillis();
                        filter.finalize(renderContext, resource, this);
                        if (logger.isDebugEnabled()) { 
                        	logger.debug("{}: finalizing filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error during finalizing of filter", e);
                }
            }
            popAttributes(renderContext.getRequest());
        }

        return out;
    }

    public void pushAttribute(HttpServletRequest request, String key, Object value) {
        if (!oldPropertiesMap.containsKey(key)) {
            oldPropertiesMap.put(key, request.getAttribute(key));
        }
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

    /**
     * Returns an unmodifiable view of the list of filters, used by this chain.
     * 
     * @return an unmodifiable view of the list of filters, used by this chain
     */
    @SuppressWarnings("unchecked")
    public List<RenderFilter> getFilters() {
        return UnmodifiableList.decorate(filters);
    }
}
