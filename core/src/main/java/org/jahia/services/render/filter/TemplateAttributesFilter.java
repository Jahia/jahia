/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.AreaResourceCacheKeyPartGenerator;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.i18n.ResourceBundles;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Module filter for parameter resolution.
 * User: toto
 * Date: Jan 18, 2010
 * Time: 3:59:45 PM
 */
public class TemplateAttributesFilter extends AbstractFilter {

    public static final String FORCED_LOCALE_ATTRIBUTE = "org.jahia.utils.i18n.forceLocale";
    public static final String AREA_RESOURCE = "areaResource";

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        final JCRSiteNode site = context.getSite();
        final JahiaTemplatesPackage templatePackage = site.getTemplatePackage();
        if (templatePackage == null) {
            throw new ItemNotFoundException("Couldn't find the template associated with site " + site.getName() + ". Please check that all its dependencies are started.");
        }

        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

        // Resolve params
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Serializable> moduleParams = resource.getModuleParams();
        for (Map.Entry<String, Serializable> entry : moduleParams.entrySet()) {
            String key = entry.getKey();
            params.put(key, entry.getValue());
        }

        /*
          TODO BACKLOG-6561: we should avoid that, if we remove the AggregateCacheFilter some day
              this is used to reset cache request attributes that should not be used
              like request attribute "expiration" that is used to set an expiration on error fragment
              It's not used anymore with new CacheFilter implementation
        */
        ExtendedNodeType cache = NodeTypeRegistry.getInstance().getNodeType("jmix:cache");
        overrideProperties(node, params, moduleParams, cache);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            chain.pushAttribute(request, entry.getKey(), entry.getValue());
        }

        Script script = (Script) request.getAttribute("script");
        Locale locale = (Locale) request.getAttribute(FORCED_LOCALE_ATTRIBUTE);
        locale = (locale != null ? locale : resource.getLocale());
        chain.pushAttribute(
                context.getRequest(),
                Config.FMT_LOCALIZATION_CONTEXT + ".request",
                new LocalizationContext(ResourceBundles.get(templatePackage.getResourceBundleName(), script.getView()
                        .getModule(), locale),locale));

        // get areaResourcePath if defined.
        String areaResourcePath = (String) request.getAttribute(AreaResourceCacheKeyPartGenerator.SAVED_AREA_PATH);
        if (areaResourcePath != null && !context.isContributionMode()) {
            chain.pushAttribute(request, AREA_RESOURCE, resource.getNode().getSession().getNode(areaResourcePath));
        }
        return null;
    }



    private void overrideProperties(JCRNodeWrapper node, Map<String, Object> params, Map<String, Serializable> moduleParams,
                                    ExtendedNodeType mixin) throws RepositoryException {
        Map<String, ExtendedPropertyDefinition> props = mixin.getDeclaredPropertyDefinitionsAsMap();
        for (String key : props.keySet()) {
            overrideProperties(node, params, moduleParams, key);
        }
    }

    private void overrideProperties(JCRNodeWrapper node, Map<String, Object> params, Map<String, Serializable> moduleParams,
                                    String key) throws RepositoryException {
        if (!key.equals("*")) {
            String pkey = StringUtils.substringAfter(key, ":");
            if (!moduleParams.containsKey("forced" + StringUtils.capitalize(pkey))) {
                if (node.hasProperty(key)) {
                    params.put(pkey, node.getProperty(key).getString());
                } else {
                    params.put(pkey, null);
                }
            }
        }
    }

}
