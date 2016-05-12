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
package org.jahia.services.render.filter.cache;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);

    private static final String KEY_PART_DELIMITER = "@@";

    // default part generators coming from the core, they are registered in the afterPropertiesSet
    private List<CacheKeyPartGenerator> partGenerators;

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
            args.add(generator.getValue(resource, renderContext, properties));
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
        return key;
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
            newArgs[index] = keyPartGenerator.replacePlaceholders(renderContext, args[index]);
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
            res[ index ++ ] = key.substring(start, end);
            start = end + 2;
        }
        res[ index ++ ] = key.substring(start);
        while (index < res.length) res[ index ++ ] = "";

        if(res.length != partGeneratorsByKey.size( )) {
            throw new IllegalStateException("Mismatched number of parts in key");
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
            updatedRequestParameters.append(",cacheinfo,moduleinfo");
        }
        properties.put("cache.requestParameters", updatedRequestParameters.toString());

        // cache expiration lookup by order : request attribute -> node -> view -> -1 (forever in cache realm, 4 hours)
        String viewExpiration = properties.getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION);
        final Object requestExpiration = request.getAttribute("expiration");
        if (requestExpiration != null) {
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
        this.partGenerators = partGenerators;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // register all the part generators that come from the core
        if (partGenerators != null && partGenerators.size() > 0) {
            for (CacheKeyPartGenerator partGenerator : partGenerators) {
                registerPartGenerator(partGenerator);
            }
        }

        // empty the list of core part generators as they should not be used after this invocation of bean initialization
        // only part generators that have been registered are available.
        partGenerators = null;
    }
}
