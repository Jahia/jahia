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
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;

import javax.servlet.http.HttpServletRequest;

/**
 * Add node related attributes in request, this was done after the cache filter because the node related info are not needed
 * if fragment is in cache.
 *
 * Before cache refactoring this operations was done in the BaseAttributesFilter, but we move this here to avoid
 * reading the node before the cache filter
 *
 * Created by jkevan on 27/04/2016.
 */
public class NodeAttributesFilter extends AbstractFilter{
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        // calling resource.safeLoadNode() here will load the node from jcr and store it in resource, if resource is lazy
        JCRNodeWrapper node = resource.safeLoadNode();
        if (node == null) {
            // Node is not available anymore, return empty content for this fragment
            // TODO throw NodeNotFoundException ?
            return StringUtils.EMPTY;
        }
        HttpServletRequest request = renderContext.getRequest();

        chain.pushAttribute(request, "workspace", node.getSession().getWorkspace().getName());
        chain.pushAttribute(request, "currentWorkspace", node.getSession().getWorkspace().getName());

        final Script script = resource.getScript(renderContext);
        if (script != null) {
            chain.pushAttribute(request, "script", script);
            chain.pushAttribute(request, "scriptInfo", script.getView().getInfo());
        } else {
            chain.pushAttribute(request, "script", null);
            chain.pushAttribute(request, "scriptInfo", null);
        }

        if (!Resource.CONFIGURATION_INCLUDE.equals(resource.getContextConfiguration())) {
            chain.pushAttribute(request, "currentNode", node);
        }

        return null;
    }
}
