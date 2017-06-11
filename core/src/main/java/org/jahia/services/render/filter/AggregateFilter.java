/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.PathCacheKeyPartGenerator;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Stack;

/**
 * Aggregate render filter, in charge of aggregating fragment by resolving sub fragments;
 * The flow of aggregation is done in multiple set
 *
 * 1  - The prepare will generate a fragment A by returning null and letting the next filters do the rendering
 *
 * 2  - Next filters can start new render chains, it's the case of the ModuleTag for example,
 *      so we will pass again in the prepare() for each render chains start by next filters,
 *      but this render chain are identify as sub fragment of fragment A. (identification is explain after)
 *
 * 3  - When a sub fragment B is identify, the chain is stop and a placeholder is return:
 *      <jahia_esi:include src="FRAGMENT B KEY"></jahia_esi:include>
 *      Next filters are not execute.
 *
 * 4  - The render of fragment A is finished when we arrive in the execute() function for fragment A.
 *
 * 5  - At this state HTML of the fragment A is ready, and contain the fragment B placeholder:
 *      <jahia_esi:include src="FRAGMENT B KEY"></jahia_esi:include>
 *
 * 6  - The Aggregation of the sub fragments for fragment A can start.
 *
 * 7  - We iterate on each sub fragments available in fragment A and start a new render chain for each
 *
 * 8  - A new render chain will be start for fragment B
 *
 * 9  - During aggregation we already have fragment B key because it's contain by the jahia_esi:include tag from
 *      fragment A. So we pass it to the new render chain using moduleParams so we don't have to calculate it again.
 *      This how we can identify a new render chain coming from aggregation and we are not in case (2)
 *
 * Created by jkevan on 12/04/2017.
 */
public class AggregateFilter extends AbstractFilter {

    private static final Logger logger = LoggerFactory.getLogger(AggregateFilter.class);

    private static final String ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String ESI_TAG_END = "\"></jahia_esi:include>";
    private static final int ESI_TAG_END_LENGTH = ESI_TAG_END.length();
    private static final String RENDERING_TIMER = "aggregateFilter.rendering.timer";

    // Use to store the current fragment key in the module map,
    // so it can be use by next filters only in the current render chain
    public static final String RENDERING_KEY = "aggregateFilter.rendering.key";

    // Use to store the current fragment final key in the module map,
    // so it can be use by next filters only in the current render chain
    public static final String RENDERING_FINAL_KEY = "aggregateFilter.rendering.final.key";

    // aggregating key, use to communicate the key of a fragment between render chains when a new render chain is started by aggregation
    public static final String AGGREGATING_KEY = "aggregateFilter.aggregating.key";

    // flag put in prepare when we are aggregating subfragment
    public static final String AGGREGATING = "aggregateFilter.aggregating";

    // Stack of resources, used to avoid fragment recursion and infinite loop
    public static final String RESOURCES_STACK = "aggregateFilter.resourcesStack";

    // if this parameter is set to true in the request attributes, the aggregation is skipped
    public static final String SKIP_AGGREGATION = "aggregateFilter.skip";

    private CacheKeyGenerator keyGenerator;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        if (skipAggregation(renderContext.getRequest())) {
            return null;
        }

        HttpServletRequest request = renderContext.getRequest();

        boolean aggregating = resource.getModuleParams().get(AGGREGATING_KEY) != null;
        boolean isPageResource = Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration());

        // render chain haven't been start by a configuration page, aggregation can't be done
        if (!isPageResource && request.getAttribute(RESOURCES_STACK) == null) {
            return null;
        }

        // Generates the key of the requested fragment. If we are currently aggregating a sub-fragment we already have the key
        // in the module params. If not, the KeyGenerator will create a key based on the request (resource and context).
        String key = aggregating ? (String) resource.getModuleParams().remove(AGGREGATING_KEY) :
                keyGenerator.generate(resource, renderContext, keyGenerator.getAttributesForKey(renderContext, resource));

        if (isPageResource || aggregating) {
            // we are on a main resource or aggregating a new fragment

            // Generate final key every time
            String finalKey = keyGenerator.replacePlaceholdersInCacheKey(renderContext, key);

            // handle key stacks to avoid fragment include recursion
            if (isPageResource) {
                // we are on main resource, initialize keys stack
                request.setAttribute(RESOURCES_STACK, new Stack<>());
            } else {
                // we are aggregating, do a security recursion check and push key in stack if it's ok
                @SuppressWarnings("unchecked")
                Stack<Resource> resourcesStack = (Stack<Resource>) request.getAttribute(RESOURCES_STACK);
                if(resourcesStack.contains(resource)) {
                    // recursion detected
                    logger.warn("Loop detected while rendering resource {}. Please check your content structure and references.", resource.getPath());
                    if (!Constants.LIVE_WORKSPACE.equals(renderContext.getMode())) {
                        return MessageFormat.format(Messages.getInternal("label.render.loop", renderContext.getUILocale()), resource.getPath());
                    }
                    return StringUtils.EMPTY;
                } else {
                    // no recursion detected push the key of current fragment in the stack, and continue
                    resourcesStack.push(resource);
                }
            }

            // Store infos in moduleMap
            @SuppressWarnings("unchecked")
            Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");
            moduleMap.put(RENDERING_KEY, key);
            moduleMap.put(RENDERING_FINAL_KEY, finalKey);
            moduleMap.put(RENDERING_TIMER, System.currentTimeMillis());
            moduleMap.put(AGGREGATING, aggregating);

            logger.debug("Rendering fragment {} with key {}", resource.getPath(), key);

            // Continue the chain to the next filter
            return null;
        } else {
            // we are rendering a sub fragment, break the chain to return the placeholder
            return ESI_TAG_START + key + ESI_TAG_END;
        }
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        if (skipAggregation(renderContext.getRequest())) {
            return previousOut;
        }

        HttpServletRequest request =  renderContext.getRequest();

        // no keys stack, no aggregation
        if (request.getAttribute(RESOURCES_STACK) == null) {
            return previousOut;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");

        if (Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration()) || moduleMap.get(AGGREGATING) == Boolean.TRUE) {
            // we are on a main resource or aggregating a new fragment

            if (logger.isDebugEnabled()) {
                long start = (Long) moduleMap.get(RENDERING_TIMER);
                logger.debug("Rendered fragment for {} took {} ms.", resource.getPath(), System.currentTimeMillis() - start);
            }

            return aggregateContent(previousOut, renderContext);
        } else {
            // we are rendering a sub fragment, break the chain to return the placeholder
            return previousOut;
        }
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain renderChain) {

        if (skipAggregation(renderContext.getRequest())) {
            return;
        }

        HttpServletRequest request = renderContext.getRequest();

        // no keys stack, no aggregation, nothing to clean
        if(request.getAttribute(RESOURCES_STACK) == null) {
            return;
        }

        // we are finalizing on a main resource, remove keys stack
        if (Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration())) {
            request.removeAttribute(RESOURCES_STACK);
        }

        // we are finalizing on a sub fragment aggregation, pop the key from the stack
        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");
        if (moduleMap.get(AGGREGATING) == Boolean.TRUE) {
            @SuppressWarnings("unchecked")
            Stack<String> keysStack = (Stack<String>) request.getAttribute(RESOURCES_STACK);
            keysStack.pop();
        }
    }

    /**
     * Aggregate the content that are inside the fragment to get a full HTML content with all sub modules embedded.
     *
     * @param content The fragment
     * @param renderContext The render context
     */
    protected String aggregateContent(String content, RenderContext renderContext) throws RenderException {
        int esiTagStartIndex = content.indexOf(ESI_TAG_START);
        if (esiTagStartIndex == -1) {
            return content;
        } else {
            StringBuilder sb = new StringBuilder(content);
            while (esiTagStartIndex != -1) {
                int esiTagEndIndex = sb.indexOf(ESI_TAG_END, esiTagStartIndex);
                if (esiTagEndIndex != -1) {
                    String replacement = generateContent(renderContext, sb.substring(esiTagStartIndex + ESI_TAG_START.length(), esiTagEndIndex));
                    if (replacement == null) {
                        replacement = "";
                    }
                    sb.replace(esiTagStartIndex, esiTagEndIndex + ESI_TAG_END_LENGTH, replacement);
                    esiTagStartIndex = sb.indexOf(ESI_TAG_START, esiTagStartIndex + replacement.length());
                } else {
                    // no closed esi end tag found
                    return sb.toString();
                }
            }
            return sb.toString();
        }
    }

    /**
     * Generates content for a sub fragment.
     *
     * @param renderContext The render context
     * @param key The key of the fragment to generate
     */
    protected String generateContent(RenderContext renderContext, String key) throws RenderException {

        try {

            // Parse the key to get all separate key attributes like node path and template
            Map<String, String> keyAttrs = keyGenerator.parse(key);

            // Create lazy resource
            String path = StringUtils.replace(keyAttrs.get("path"), PathCacheKeyPartGenerator.MAIN_RESOURCE_KEY, StringUtils.EMPTY);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getWorkspace(), LanguageCodeConverters.languageCodeToLocale(keyAttrs.get("language")), renderContext.getFallbackLocale());

            String canonicalPath = keyAttrs.get("canonicalPath");
            canonicalPath = StringUtils.isEmpty(canonicalPath) ? path : canonicalPath;

            Resource resource = new Resource(path, canonicalPath, currentUserSession, keyAttrs.get("templateType"), keyAttrs.get("template"), keyAttrs.get("context"));

            // Store sub fragment key in module params of the resource to use it in prepare()
            // instead of generating the sub fragment key from scratch
            resource.getModuleParams().put(AGGREGATING_KEY, key);

            String content;
            Map<String, Object> original = keyGenerator.prepareContextForContentGeneration(keyAttrs, resource, renderContext);
            try {
                // Dispatch to the render service to generate the content
                content = RenderService.getInstance().render(resource, renderContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("fragment generated for resource {}", resource);
                    if (SettingsBean.getInstance().isDevelopmentMode()) {
                        logger.debug(content);
                    }
                }
            } finally {
                keyGenerator.restoreContextAfterContentGeneration(keyAttrs, resource, renderContext, original);
            }

            return content;

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    /**
     * Utility method to check if the aggregation is skipped.
     * @param request is the current request
     * @return
     */
    public static boolean skipAggregation(ServletRequest request) {
        return BooleanUtils.isTrue((Boolean)request.getAttribute(SKIP_AGGREGATION));
    }

}
