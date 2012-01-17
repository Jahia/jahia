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

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.scripting.Script;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * Stores the required request parameters before evaluating the template and restores original after.
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class BaseAttributesFilter extends AbstractFilter {
    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

        request.setAttribute("renderContext", context);
        try {
            final Script script = service.resolveScript(resource, context);
            chain.pushAttribute(request, "script", script);
            chain.pushAttribute(request, "scriptInfo", script.getView().getInfo());
        } catch (TemplateNotFoundException e) {
            chain.pushAttribute(request, "script", null);
            chain.pushAttribute(request, "scriptInfo", null);
        }
        chain.pushAttribute(request, "workspace", node.getSession().getWorkspace().getName());
        chain.pushAttribute(request, "currentResource", resource);
        chain.pushAttribute(request, "currentUser", context.getMainResource().getNode().getSession().getUser());
        chain.pushAttribute(request, "currentAliasUser", context.getMainResource().getNode().getSession().getAliasedUser());
        if (!Resource.CONFIGURATION_INCLUDE.equals(resource.getContextConfiguration())) {
            chain.pushAttribute(request, "currentNode", node);
            chain.pushAttribute(request, "url", new URLGenerator(context, resource));
        }

        return null;
    }

}
