/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);

    private static final String KEY_PART_DELIMITER = "@@";

    private static final String KEY_PART_DELIMITER_ESCAPE = "&dblAt;";
    private static final String AMPERSAND_ESCAPE = "&amp;";

    // All part generators coming from the core and the modules. This map defines the order of elements in the key.
    // It's important to maintain this order everywhere in key parsing, construction, transformation.
    // You can rely on this map iteration order to access elements in a given key.
    private Map<String, CacheKeyPartGenerator> partGeneratorsByKey = new LinkedHashMap<>();

    /**
     * Register a part generator, if part generator already exist with that key it will be replaced
     * @param partGenerator the part generator to be registered
     */
    public void registerPartGenerator(CacheKeyPartGenerator partGenerator) {
        partGeneratorsByKey.put(partGenerator.getKey(), partGenerator);
    }

    /**
     * Unregister a part generator
     * @param partGenerator the part generator to be unregistered
     */
    public void unregisterPartGenerator(CacheKeyPartGenerator partGenerator) {
        partGeneratorsByKey.remove(partGenerator.getKey());
    }

    @Override
    public String generate(Resource resource, RenderContext renderContext, Properties properties) {
        List<String> args = new LinkedList<>();
        for (CacheKeyPartGenerator generator : partGeneratorsByKey.values()) {
            String value = generator.getValue(resource, renderContext, properties);
            args.add(encodeKeyPart(value));
        }
        return StringUtils.join(args, KEY_PART_DELIMITER);
    }

    @Override
    public Map<String, String> parse(String key) {
        String[] values = getSplit(key);
        Map<String, String> result = new LinkedHashMap<>(partGeneratorsByKey.size());
        int index = 0;
        for (String partGeneratorKey : partGeneratorsByKey.keySet()) {
            result.put(partGeneratorKey, values[index] == null || values[index].equals("null") ? null : values[index]);
            index++;
        }
        return result;
    }

    @Override
    @Deprecated // not used anymore
    public String replaceField(String key, String fieldName, String newValue) {

        String[] args = getSplit(key);

        Integer index = null;
        int i = 0;
        for (String partGeneratorKey : partGeneratorsByKey.keySet()) {
            if (partGeneratorKey.equals(fieldName)) {
                index = i;
                break;
            }
            i++;
        }

        args[index] = encodeKeyPart(newValue);
        return StringUtils.join(args, KEY_PART_DELIMITER);
    }

    @Override
    public CacheKeyPartGenerator getPartGenerator(String field) {
        return partGeneratorsByKey.get(field);
    }

    @Override
    public String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        String[] args = getSplit(key);
        String[] newArgs = new String[args.length];
        int index = 0;
        for (CacheKeyPartGenerator keyPartGenerator : partGeneratorsByKey.values()) {
            String value = keyPartGenerator.replacePlaceholders(renderContext, args[index]);
            newArgs[index] = encodeKeyPart(value);
            index++;
        }
        return StringUtils.join(newArgs, KEY_PART_DELIMITER);
    }

    /**
     * Split the given key into parts, the order of the array return is always the same as the part generators registration order
     *
     * @param key the given key to be split
     * @return the array of key parts
     */
    private String[] getSplit(String key) {

        String[] res = new String[partGeneratorsByKey.size()];
        int index = 0;
        int start = 0;
        int end;
        while ((end = key.indexOf(KEY_PART_DELIMITER, start)) > -1) {
            String value = key.substring(start, end);
            res[index++] = decodeKeyPart(value);
            start = end + KEY_PART_DELIMITER.length();
        }
        res[index++] = key.substring(start);
        while (index < res.length) {
            res[index++] = "";
        }

        return res;
    }

    @Override
    public Map<String, Object> prepareContextForContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext) {
        Map<String, Object> original = new HashMap<>();
        for (Map.Entry<String, String> entry : keyParts.entrySet()) {
            CacheKeyPartGenerator partGenerator = partGeneratorsByKey.get(entry.getKey());
            if (partGenerator instanceof RenderContextTuner) {
                original.put(entry.getKey(), ((RenderContextTuner) partGenerator).prepareContextForContentGeneration(entry.getValue(), resource, renderContext));
            }
        }
        return original;
    }

    @Override
    public void restoreContextAfterContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext, Map<String, Object> original) {
        for (Map.Entry<String, String> entry : keyParts.entrySet()) {
            CacheKeyPartGenerator partGenerator = partGeneratorsByKey.get(entry.getKey());
            if (partGenerator instanceof RenderContextTuner) {
                ((RenderContextTuner) partGenerator).restoreContextAfterContentGeneration(entry.getValue(), resource, renderContext, original.get(entry.getKey()));
            }
        }
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
     */
    @Override
    public Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException {

        HttpServletRequest request = renderContext.getRequest();
        final Script script = resource.getScript(renderContext);
        final JCRNodeWrapper node = resource.safeLoadNode();
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
                Script listScript = RenderService.getInstance().resolveScript(listLoader, renderContext);
                properties.putAll(listScript.getView().getProperties());
            } catch (TemplateNotFoundException e) {
                logger.error("Cannot find loader script for list " + node.getPath(), e);
            }
        }

        if (node.hasProperty(CacheUtils.NODE_PROPERTY_CACHE_PER_USER)) {
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER, node.getProperty(CacheUtils.NODE_PROPERTY_CACHE_PER_USER).getString());
        }
        if (isBound) {
            // TODO check this, if the component is a bound component don't mean that it's always bound to the main resource
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
            updatedRequestParameters.append(",moduleinfo");
        }
        properties.put("cache.requestParameters", updatedRequestParameters.toString());

        // cache expiration lookup by order : request attribute -> node -> view -> -1 (forever in cache realm, 4 hours)
        String viewExpiration = properties.getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION);
        final Object requestExpiration = request.getAttribute("expiration");
        if (requestExpiration != null) {
            // TODO BACKLOG-6561: we should avoid that, the day we remove AggregateCacheFilter, we can remove this one
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION, requestExpiration);
        } else if (node.hasProperty("j:expiration")) {
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION, node.getProperty("j:expiration").getString());
        } else if (viewExpiration != null) {
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION, viewExpiration);
        } else {
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION, "-1");
        }

        String propertiesScript = properties.getProperty("cache.propertiesScript");
        if (propertiesScript != null) {
            Resource propsResource = new Resource(node, resource.getTemplateType(), propertiesScript, Resource.CONFIGURATION_INCLUDE);
            try {
                Script propsScript = RenderService.getInstance().resolveScript(propsResource, renderContext);
                try {
                    request.setAttribute("cacheProperties", properties);
                    propsScript.execute(propsResource, renderContext);
                } catch (RenderException e) {
                    logger.error("Cannot execute script",e);
                } finally {
                    request.removeAttribute("cacheProperties");
                }
            } catch (TemplateNotFoundException e) {
                logger.error("Cannot find cache properties script " + propertiesScript + " for the node " + node.getPath(), e);
            }
        }

        return properties;
    }

    public void setPartGenerators(List<CacheKeyPartGenerator> partGenerators) {
        for (CacheKeyPartGenerator partGenerator : partGenerators) {
            registerPartGenerator(partGenerator);
        }
    }

    private static String encodeKeyPart(String keyPart) {
        if (keyPart == null) {
            return null;
        }
        return StringUtils.replace(StringUtils.replace(keyPart, "&", AMPERSAND_ESCAPE), KEY_PART_DELIMITER, KEY_PART_DELIMITER_ESCAPE);
    }

    private static String decodeKeyPart(String keyPart) {
        return StringUtils.replace(StringUtils.replace(keyPart, KEY_PART_DELIMITER_ESCAPE, KEY_PART_DELIMITER), AMPERSAND_ESCAPE, "&");
    }
}
