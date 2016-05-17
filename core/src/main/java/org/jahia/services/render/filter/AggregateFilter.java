/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.PathCacheKeyPartGenerator;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

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
 * 9  - We put a flag when starting this new render chain, to say that we are aggregation and
 *      this is how we can identify that we are not in case 2 (rendering a sub fragment from next filters)
 *
 * Created by jkevan on 12/04/2016.
 */
public class AggregateFilter extends AbstractFilter {

    private static final Logger logger = LoggerFactory.getLogger(AggregateFilter.class);

    private static final String ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String ESI_TAG_END = "\"></jahia_esi:include>";
    private static final int ESI_TAG_END_LENGTH = ESI_TAG_END.length();

    // rendering flag, is use to store the current fragment key in the module map,
    // so it can be use by next filters only in the current render chain
    public static final String RENDERING = "aggregateFilter.rendering";
    // timer to debug rendering time of fragment
    public static final String RENDERING_TIMER = "aggregateFilter.rendering.timer";
    // aggregating flag, put before aggregating a sub fragment, to send the sub fragmen key to the new render chain
    // to avoid reconstruct the key for this sub fragment.
    public static final String AGGREGATING = "aggregateFilter.aggregating";
    // Security flag put in the request, The AggregateFilter is allow to aggregate fragments only
    // when first resource is in configuration PAGE
    // avoid issue when a new render chain is start on a resource in configuration MODULE, example: WeblowAction.java
    public static final String AGGREGATION_ALLOWED = "aggregateFilter.allowed";

    private CacheKeyGenerator keyGenerator;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        HttpServletRequest request = renderContext.getRequest();

        boolean aggregating = resource.getModuleParams().get(AGGREGATING) != null;
        boolean isPageResource = Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration());

        if(isPageResource) {
            // Put allowed flag
            request.setAttribute(AGGREGATION_ALLOWED, Boolean.TRUE);
        } else if (request.getAttribute(AGGREGATION_ALLOWED) == null) {
            // Not allow to aggregate
            return null;
        }

        // Generates the key of the requested fragment. If we are currently aggregating a sub-fragment we already have the key
        // in the module params. If not, the KeyGenerator will create a key based on the request (resource and context).
        // The generated key will contain temporary placeholders that will be replaced to have the final key.
        String key = aggregating ? (String) resource.getModuleParams().get(AGGREGATING) :
                keyGenerator.generate(resource, renderContext, keyGenerator.getAttributesForKey(renderContext, resource));

        if (isPageResource || aggregating) {
            // we are on a main resource or aggregating a new fragment

            @SuppressWarnings("unchecked")
            Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");

            moduleMap.put(RENDERING, key);
            moduleMap.put(RENDERING_TIMER, System.currentTimeMillis());

            logger.debug("Rendering node " + resource.getPath());
            logger.debug("Aggregate filter for {}, key with placeholders: {}", resource.getPath(), key);

            // fragment key is in moduleMap, continue the chain to the next filter
            return null;
        } else {
            // we are rendering a sub fragment, break the chain to return the placeholder
            return ESI_TAG_START + key + ESI_TAG_END;
        }
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        HttpServletRequest request = renderContext.getRequest();

        // if not allow to aggregate, just leave
        if(request.getAttribute(AGGREGATION_ALLOWED) == null) {
            return previousOut;
        }

        if (Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration()) || resource.getModuleParams().get(AGGREGATING) != null) {
            // we are on a main resource or aggregating a new fragment

            @SuppressWarnings("unchecked")
            Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");

            if (logger.isDebugEnabled()) {
                long start = (Long) moduleMap.get(RENDERING_TIMER);
                logger.debug("AggregateFilter for {}  took {} ms.", resource.getPath(), System.currentTimeMillis() - start);
            }

            logger.debug("Now aggregating subcontent for {}, key = {}", resource.getPath(), moduleMap.get(RENDERING));
            return aggregateContent(previousOut, renderContext);
        } else {
            // we are rendering a sub fragment, break the chain to return the placeholder
            return previousOut;
        }
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain renderChain) {
        if (Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration())) {
            renderContext.getRequest().removeAttribute(AGGREGATION_ALLOWED);
        }
    }

    /**
     * Aggregate the content that are inside the fragment to get a full HTML content with all sub modules embedded.
     *
     * @param content The fragment
     * @param renderContext The render context
     */
    protected String aggregateContent(String content, RenderContext renderContext) {
        int esiTagStartIndex = content.indexOf(ESI_TAG_START);
        if (esiTagStartIndex == -1) {
            return content;
        } else {
            StringBuilder sb = new StringBuilder(content);
            while (esiTagStartIndex != -1) {
                int esiTagEndIndex = sb.indexOf(ESI_TAG_END, esiTagStartIndex);
                if (esiTagEndIndex != -1) {
                    String key = sb.substring(esiTagStartIndex + ESI_TAG_START.length(), esiTagEndIndex);
                    try {
                        String replacement = generateContent(renderContext, key);
                        if (replacement == null) {
                            replacement = "";
                        }
                        sb.replace(esiTagStartIndex, esiTagEndIndex + ESI_TAG_END_LENGTH, replacement);
                        esiTagStartIndex = sb.indexOf(ESI_TAG_START, esiTagStartIndex + replacement.length());
                    } catch (RenderException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
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
            Resource resource = new Resource(path, currentUserSession, keyAttrs.get("templateType"), keyAttrs.get("template"), keyAttrs.get("context"));

            // Store sub fragment key in module params of the resource to use it in prepare()
            // instead of generating the sub fragment key from scratch
            resource.getModuleParams().put(AGGREGATING, key);

            String content;
            Map<String, Object> original = keyGenerator.prepareContextForContentGeneration(keyAttrs, resource, renderContext);
            try {
                // Dispatch to the render service to generate the content
                content = RenderService.getInstance().render(resource, renderContext);
                if (StringUtils.isBlank(content) && renderContext.getRedirect() == null) {
                    logger.error("Empty generated content for key " + key);
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
}
