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

import javax.jcr.RepositoryException;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);

    private List<CacheKeyPartGenerator> partGenerators;
    private LinkedHashMap<String, Integer> fields;

    public List<CacheKeyPartGenerator> getPartGenerators() {
        return partGenerators;
    }

    public void setPartGenerators(List<CacheKeyPartGenerator> partGenerators) {
        this.partGenerators = partGenerators;
        this.fields = new LinkedHashMap<>(17);
        int index = 0;
        for (CacheKeyPartGenerator generator : partGenerators) {
            final String key = generator.getKey();
            if (fields.containsKey(key)) {
                throw new RuntimeException("Cannot register key part generator with existing key " + key + " , " + generator);
            }
            fields.put(key, index++);
        }
    }

    @Override
    public String generate(Resource resource, RenderContext renderContext, Properties properties) {
        return StringUtils.join(getArguments(resource, renderContext, properties), "@@");
    }

    private Collection<String> getArguments(Resource resource, RenderContext renderContext, Properties properties) {
        List<String> args = new LinkedList<>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            args.add(generator.getValue(resource, renderContext, properties));
        }
        return args;
    }

    @Override
    public Map<String, String> parse(String key) {
        String[] values = getSplit(key);
        Map<String, String> result = new LinkedHashMap<>(fields.size());
        if (values.length != fields.size()) {
            throw new IllegalStateException("Mismatched number of fields and values.");
        }

        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            String value = values[entry.getValue()];
            result.put(entry.getKey(), value == null || value.equals("null") ? null : value);
        }

        return result;
    }

    @Override
    public String replaceField(String key, String fieldName, String newValue) {
        String[] args = getSplit(key);
        args[fields.get(fieldName)] = newValue;
        return StringUtils.join(args, "@@");
    }

    @Override
    public CacheKeyPartGenerator getPartGenerator(String field) {
        return partGenerators.get(fields.get(field));
    }

    @Override
    public String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        String[] args = getSplit(key);
        String[] newArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            newArgs[i] = partGenerators.get(i).replacePlaceholders(renderContext,value);
        }
        String s = StringUtils.join(newArgs,"@@");
        return s;
    }

    private String[] getSplit(String key) {
        String[] res = new String[fields.size()];
        int index = 0;
        int start = 0;
        int end;
        while ((end = key.indexOf("@@",start)) > -1) {
            res[ index ++ ] = key.substring(start,end);
            start = end + 2;
        }
        res[ index ++ ] = key.substring(start);
        while (index < res.length) res[ index ++ ] = "";
        return res;
    }

    @Override
    public Map<String, Object> prepareContentForContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext) {
        Map<String, Object> original = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, String> entry : keyParts.entrySet()) {
            CacheKeyPartGenerator partGenerator = partGenerators.get(i);
            if (partGenerator instanceof  ContextModifierCacheKeyPartGenerator) {
                original.put(entry.getKey(), ((ContextModifierCacheKeyPartGenerator) partGenerator).prepareContentForContentGeneration(entry.getValue(), resource, renderContext));
            }
            i++;
        }
        return original;
    }

    @Override
    public void restoreContextAfterContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext, Map<String, Object> original) {
        int i = 0;
        for (Map.Entry<String, String> entry : keyParts.entrySet()) {
            CacheKeyPartGenerator partGenerator = partGenerators.get(i);
            if (partGenerator instanceof  ContextModifierCacheKeyPartGenerator) {
                ((ContextModifierCacheKeyPartGenerator) partGenerator).restoreContextAfterContentGeneration(entry.getValue(), resource, renderContext, original.get(entry.getKey()));
            }
            i++;
        }
    }

    @Override
    public Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException {

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
                Script s = RenderService.getInstance().resolveScript(listLoader, renderContext);
                properties.putAll(s.getView().getProperties());
            } catch (TemplateNotFoundException e) {
                logger.error("Cannot find loader script for list " + node.getPath(), e);
            }
        }

        if (node.hasProperty(CacheUtils.NODE_PROPERTY_CACHE_PER_USER)) {
            properties.put(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER, node.getProperty(CacheUtils.NODE_PROPERTY_CACHE_PER_USER).getString());
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
        String viewExpiration = properties.getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION);
        final Object requestExpiration = renderContext.getRequest().getAttribute("expiration");
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
            Resource props = new Resource(node, resource.getTemplateType(), propertiesScript, Resource.CONFIGURATION_INCLUDE);
            try {
                Script s = RenderService.getInstance().resolveScript(props, renderContext);
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
}
