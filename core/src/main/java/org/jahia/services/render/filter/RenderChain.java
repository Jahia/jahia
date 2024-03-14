/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter;

import static org.jahia.services.render.RenderInfo.FilterEvent.*;

import org.apache.commons.collections.list.UnmodifiableList;
import org.jahia.exceptions.RenderTimeLimitExceededException;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderInfo;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Main pipeline that generates output for rendering.
 *
 * Date: Nov 24, 2009
 * Time: 12:33:52 PM
 */
public class RenderChain {

    private static final Logger logger = LoggerFactory.getLogger(RenderChain.class);

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
        doSortFilters();
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
        RenderInfo.pushResource(resource);

        String out = null;
        int index = 0;

        if (logger.isTraceEnabled()) {
            logger.trace("Configured filters:");
            for (RenderFilter filter : filters) {
                logger.trace("  " + filter.getClass().getName());
            }
        }

        String nodePath = "empty resource";
        if (resource != null) {
            nodePath = resource.getNodePath();
        }
        Integer finalFilterIndex = null;
        try {
            for (; index < filters.size() && out == null && renderContext.getRedirect() == null; index++) {
                RenderFilter filter = filters.get(index);
                if (filter.areConditionsMatched(renderContext, resource)) {
                    long timer = System.currentTimeMillis();
                    finalFilterIndex = index;
                    RenderInfo.addFilterEvent(filter, TYPE_PREPARE);
                    out = filter.prepare(renderContext, resource, this);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: prepare filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
            index--;
            for (; index >= 0 && renderContext.getRedirect() == null; index--) {
                RenderFilter filter = filters.get(index);
                if (filter.areConditionsMatched(renderContext, resource)) {
                    long timer = System.currentTimeMillis();
                    RenderInfo.addFilterEvent(filter, TYPE_EXECUTE);
                    out = filter.execute(out, renderContext, resource, this);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: execute filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
        } catch (RenderTimeLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            out = null;
            for (index++; index > 0 && renderContext.getRedirect() == null && out == null; index--) {
                RenderFilter filter = filters.get(index-1);
                if (filter.areConditionsMatched(renderContext, resource)) {
                    long timer = System.currentTimeMillis();
                    RenderInfo.addFilterEvent(filter, TYPE_GET_ERROR_CONTENT);
                    out = filter.getContentForError(renderContext, resource, this, e);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: handling error for filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                    }
                }
            }
            if (out == null) {
                if (renderContext.isEditMode()) {
                    logger.error("Error while rendering the resource: " + resource, e);
                    out = "<pre>An error has occurred.</pre>";
                } else {
                    throw new RenderFilterException(e);
                }
            } else {
                logger.error("Error while rendering the resource: " + resource, e);
            }
        } finally {
            if(finalFilterIndex !=  null) {
                for (index = 0; index <= finalFilterIndex; index++) {
                    try {
                        RenderFilter filter = filters.get(index);
                        if (filter.areConditionsMatched(renderContext, resource)) {
                            long timer = System.currentTimeMillis();
                            RenderInfo.addFilterEvent(filter, TYPE_FINALIZE);
                            filter.finalize(renderContext, resource, this);
                            if (logger.isDebugEnabled()) {
                                logger.debug("{}: finalizing filter {} done in {} ms", new Object[] {nodePath, filter.getClass().getName(), System.currentTimeMillis() - timer});
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Something wrong happened during finalizing of filter: {}, the render filters might have been modified externally, set that class in DEBUG log level for more detail ", e.getMessage());
                        if (logger.isDebugEnabled()) {
                            logger.debug("Error during finalizing of filter", e);
                        }
                    }
                }
            }
            popAttributes(renderContext.getRequest());
            RenderInfo.popResource();
        }

        return out;
    }

    public void doSortFilters(){
        Collections.sort(this.filters);
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
