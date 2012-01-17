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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Module filter for parameter resolution.
 * User: toto
 * Date: Jan 18, 2010
 * Time: 3:59:45 PM
 */
public class TemplateAttributesFilter extends AbstractFilter {

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

        // Resolve params
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Serializable> moduleParams = resource.getModuleParams();
        for (Map.Entry<String, Serializable> entry : moduleParams.entrySet()) {
            String key = entry.getKey();
            params.put(key, entry.getValue());
        }

        ExtendedNodeType cache = NodeTypeRegistry.getInstance().getNodeType("jmix:cache");
        overrideProperties(node, params, moduleParams, cache);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            chain.pushAttribute(request, entry.getKey(), entry.getValue());
        }

        if (!resource.getContextConfiguration().equals(Resource.CONFIGURATION_INCLUDE)) {
            chain.pushAttribute(request, "moduleMap", new HashMap());
        }

        Script script = (Script) request.getAttribute("script");
        chain.pushAttribute(context.getRequest(), Config.FMT_LOCALIZATION_CONTEXT + ".request", new LocalizationContext(
                new JahiaResourceBundle(resource.getLocale(), script.getView().getModule().getName()),
                resource.getLocale()));
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
