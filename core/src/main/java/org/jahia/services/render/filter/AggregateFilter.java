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
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.cache.CacheFilter;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.PathCacheKeyPartGenerator;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
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
        // Generates the key of the requested fragment. The KeyGenerator will create a key based on the request
        // (resource and context) and the cache properties. The generated key will contains temporary placeholders
        // that will be replaced to have the final key.
        Properties properties = getAttributesForKey(renderContext, resource);
        String key = keyGenerator.generate(resource, renderContext, properties);

        if (renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering") != null) {
            renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering.submodule", key);
            return CACHE_ESI_TAG_START + key + CACHE_ESI_TAG_END;
        }

        logger.debug("Rendering node " + resource.getPath());

        renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering", key);
        renderContext.getRequest().setAttribute("aggregateCacheFilter.rendering.properties", properties);
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
        renderContext.getRequest().removeAttribute("aggregateCacheFilter.rendering.properties");
        return aggregateContent(previousOut, renderContext, null);
    }

    /**
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation. The
     * cache properties may come from the script properties file, or from the jmix:cache mixin (for cache.perUser
     * only).
     * <p/>
     * If the component is a list, the properties can also come from its hidden.load script properties.
     * <p/>
     * cache.perUser : is the cache entry specific for each user. Is set by j:perUser node property or cache.perUser
     * property in script properties
     * <p/>
     * cache.mainResource : is the cache entry dependant on the main resource. Is set by cache.mainResource property
     * in script properties, or automatically set if the component is bound.
     * <p/>
     * cache.requestParameters : list of request parameter that will impact the rendering of the resource. Is set
     * by cache.requestParameters property in script properties. ec,v,cacheinfo and moduleinfo are automatically added.
     * <p/>
     * cache.expiration : the expiration time of the cache entry. Can be set by the "expiration" request attribute,
     * j:expiration node property or the cache.expiration property in script properties.
     *
     * @param renderContext
     * @param resource
     * @return
     * @throws RepositoryException
     */
    protected Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        final JCRNodeWrapper node = resource.getNode();
        boolean isBound = node.isNodeType(Constants.JAHIAMIX_BOUND_COMPONENT);
        boolean isList = node.isNodeType(Constants.JAHIAMIX_LIST);

        Properties properties = new Properties();

        if (script != null) {
            properties.putAll(script.getView().getDefaultProperties());
            properties.putAll(script.getView().getProperties());
        }

        if (isList) {
            Resource listLoader = new Resource(node, resource.getTemplateType(), "hidden.load", Resource.CONFIGURATION_INCLUDE);
            try {
                Script s = service.resolveScript(listLoader, renderContext);
                properties.putAll(s.getView().getProperties());
            } catch (TemplateNotFoundException e) {
                logger.error("Cannot find loader script for list " + node.getPath(), e);
            }
        }

        if (node.hasProperty(CacheFilter.CACHE_PER_USER_PROPERTY)) {
            properties.put(CacheFilter.CACHE_PER_USER, node.getProperty(CacheFilter.CACHE_PER_USER_PROPERTY).getString());
        }
        if (isBound) {
            // TODO check this, if the component is a binded component don't mean that it's always bind to the main ressource
            properties.put("cache.mainResource", "true");
        }

        // update requestParameters if needed
        final StringBuilder updatedRequestParameters;
        final String requestParameters = properties.getProperty("cache.requestParameters");
        if (!StringUtils.isEmpty(requestParameters)) {
            updatedRequestParameters = new StringBuilder(requestParameters + ",ec,v");
        } else {
            updatedRequestParameters = new StringBuilder("ec,v");
        }
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            updatedRequestParameters.append(",cacheinfo,moduleinfo");
        }
        properties.put("cache.requestParameters", updatedRequestParameters.toString());

        // cache expiration lookup by order : request attribute -> node -> view -> -1 (forever in cache realm, 4 hours)
        String viewExpiration = properties.getProperty(CacheFilter.CACHE_EXPIRATION);
        final Object requestExpiration = renderContext.getRequest().getAttribute("expiration");
        if (requestExpiration != null) {
            properties.put(CacheFilter.CACHE_EXPIRATION, requestExpiration);
        } else if (node.hasProperty("j:expiration")) {
            properties.put(CacheFilter.CACHE_EXPIRATION, node.getProperty("j:expiration").getString());
        } else if (viewExpiration != null) {
            properties.put(CacheFilter.CACHE_EXPIRATION, viewExpiration);
        } else {
            properties.put(CacheFilter.CACHE_EXPIRATION, "-1");
        }

        String propertiesScript = properties.getProperty("cache.propertiesScript");
        if (propertiesScript != null) {
            Resource props = new Resource(node, resource.getTemplateType(), propertiesScript, Resource.CONFIGURATION_INCLUDE);
            try {
                Script s = service.resolveScript(props, renderContext);
                try {
                    renderContext.getRequest().setAttribute("cacheProperties", properties);
                    s.execute(props, renderContext);
                } catch (RenderException e) {
                    logger.error("Cannot execute script",e);
                } finally {
                    renderContext.getRequest().removeAttribute("cacheProperties");
                }
            } catch (TemplateNotFoundException e) {
                logger.error(
                        "Cannot find cache properties script " + propertiesScript + " for the node " + node.getPath(),
                        e);
            }
        }

        return properties;
    }

    /**
     * Aggregate the content that are inside the cached fragment to get a full HTML content with all sub modules
     * embedded.
     *
     * @param cachedContent  The fragment, as it is stored in the cache
     * @param renderContext  The render context
     * @param areaIdentifier
     * @return
     */
    protected String aggregateContent(String cachedContent, RenderContext renderContext, String areaIdentifier) throws RenderException {
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
                                generateContent(renderContext, cacheKey, areaIdentifier));
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
     * @param areaIdentifier
     */
    protected String generateContent(RenderContext renderContext,
                                     String cacheKey, String areaIdentifier) throws RenderException {
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
                logger.debug("Calling render service for generating content for key " + cacheKey + " with attributes : " +
                        " areaIdentifier " + areaIdentifier);
            }

            // Prepare to dispatch to the render service - restore all area/templates atributes
            renderContext.getRequest().removeAttribute(
                    "areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));

            if (areaIdentifier != null) {
                renderContext.getRequest().setAttribute("areaListResource", currentUserSession.getNodeByIdentifier(areaIdentifier));
            }

            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), keyAttrbs.get("context"));
            Map<String, Object> previous = keyGenerator.prepareContentForContentGeneration(keyAttrbs, resource, renderContext);

            /* Fragment with full final key is not in the cache, set cache.forceGeneration parameter to avoid returning
            // a cache entry based on incomplete dependencies.
            resource.getModuleParams().put("cache.forceGeneration", true); */

            // Dispatch to the render service to generate the content
            String content = RenderService.getInstance().render(resource, renderContext);
            if (StringUtils.isBlank(content) && renderContext.getRedirect() == null) {
                logger.error("Empty generated content for key " + cacheKey + " with attributes : " +
                        " areaIdentifier " + areaIdentifier);
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
