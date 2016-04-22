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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.PathCacheKeyPartGenerator;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Aggregate render filter, in charge of aggregating fragment by resolving sub fragments
 *
 * Created by jkevan on 12/04/2016.
 */
public class AggregateFilter extends AbstractFilter{
    protected transient static final Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateFilter.class);

    private static final String CACHE_ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String CACHE_ESI_TAG_END = "\"></jahia_esi:include>";
    private static final int CACHE_ESI_TAG_END_LENGTH = CACHE_ESI_TAG_END.length();

    private CacheKeyGenerator keyGenerator;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final boolean debugEnabled = logger.isDebugEnabled();
        // Generates the key of the requested fragment. if we are currently aggregating a subfragment we already have the key
        // in the request, if not the KeyGenerator will create a key based on the request
        // (resource and context) and the cache properties. The generated key will contains temporary placeholders
        // that will be replaced to have the final key.
        String key;
        if(renderContext.getRequest().getAttribute("aggregateCacheFilter.aggregating") != null) {
            key = (String) renderContext.getRequest().getAttribute("aggregateCacheFilter.aggregating");
            renderContext.getRequest().removeAttribute("aggregateCacheFilter.aggregating");
        } else {
            key = keyGenerator.generate(resource, renderContext, keyGenerator.getAttributesForKey(renderContext, resource));
        }

        if (renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering") != null) {
            renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering.submodule", key);
            return CACHE_ESI_TAG_START + key + CACHE_ESI_TAG_END;
        }

        logger.debug("Rendering node " + resource.getPath());

        renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering", key);
        renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering.time", System.currentTimeMillis());

        if (debugEnabled) {
            logger.debug("Aggregate cache filter for {} ,  key with placeholders : {}", resource.getPath(), key);
        }

        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering.submodule") != null) {
            renderContext.getRequest().removeAttribute("aggregateCacheFilter.rendering.submodule");
            return previousOut;
        }

        String key = (String) renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering");
        logger.debug("Now aggregating subcontent for {}, key = {}", resource.getPath(), key);
        renderContext.getRequest().removeAttribute("aggregateCacheFilter.rendering");
        return aggregateContent(previousOut, renderContext);
    }

    /**
     * Aggregate the content that are inside the cached fragment to get a full HTML content with all sub modules
     * embedded.
     *
     * @param cachedContent  The fragment, as it is stored in the cache
     * @param renderContext  The render context
     * @return
     */
    protected String aggregateContent(String cachedContent, RenderContext renderContext) throws RenderException {
        int esiTagStartIndex = cachedContent.indexOf(CACHE_ESI_TAG_START);
        if(esiTagStartIndex == -1){
            return cachedContent;
        } else {
            StringBuilder sb = new StringBuilder(cachedContent);
            while (esiTagStartIndex != -1){
                int esiTagEndIndex = sb.indexOf(CACHE_ESI_TAG_END, esiTagStartIndex);
                if (esiTagEndIndex != -1) {
                    String cacheKey = sb.substring(esiTagStartIndex + CACHE_ESI_TAG_START.length(), esiTagEndIndex);
                    try {
                        esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH,
                                generateContent(renderContext, cacheKey));
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

    private int replaceInContent(StringBuilder sb, int start, int end, String replacement) {
        if (replacement == null) {
            replacement = "";
        }
        sb.replace(start, end, replacement);
        return sb.indexOf(CACHE_ESI_TAG_START, start + replacement.length());
    }

    /**
     * Generates content for a sub fragment.
     *
     * @param renderContext  The render context
     * @param cacheKey       The cache key of the fragment to generate
     */
    protected String generateContent(RenderContext renderContext, String cacheKey) throws RenderException {
        try {
            // Parse the key to get all separate key attributes like node path and template
            Map<String, String> keyAttrbs = keyGenerator.parse(cacheKey);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getWorkspace(), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            JCRNodeWrapper node;
            try {
                // Get the node associated to the fragment to generate
                node = currentUserSession.getNode(StringUtils.replace(keyAttrbs.get("path"), PathCacheKeyPartGenerator.MAIN_RESOURCE_KEY, StringUtils.EMPTY));
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node {} is no longer available." + " Replacing output with empty content.",
                            keyAttrbs.get("path"));
                }
                // Node is not found, return empty content
                return StringUtils.EMPTY;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Calling render service for generating content for key " + cacheKey);
            }

            // Prepare to dispatch to the render service - restore all area/templates atributes
            renderContext.getRequest().removeAttribute(
                    "areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));

            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), keyAttrbs.get("context"));

            Map<String, Object> previous = keyGenerator.prepareContentForContentGeneration(keyAttrbs, resource, renderContext);

            /* Fragment with full final key is not in the cache, set cache.forceGeneration parameter to avoid returning
            // a cache entry based on incomplete dependencies.
            resource.getModuleParams().put("cache.forceGeneration", true); */

            // Dispatch to the render service to generate the content
            renderContext.getRequest().setAttribute("aggregateCacheFilter.aggregating", cacheKey);
            String content = RenderService.getInstance().render(resource, renderContext);
            if (StringUtils.isBlank(content) && renderContext.getRedirect() == null) {
                logger.error("Empty generated content for key " + cacheKey);
            }

            keyGenerator.restoreContextAfterContentGeneration(keyAttrbs, resource, renderContext, previous);

            return content;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    public CacheKeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }
}
