/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.scripting.Script;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Stores the required request parameters before evaluating the template and restores original after.
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class BaseAttributesFilter extends AbstractFilter {

    public Set<String> configurationToSkipInResourceRenderedPath;

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
        boolean added = false;
        if(!configurationToSkipInResourceRenderedPath.contains(resource.getContextConfiguration())) {
            added = context.getRenderedPaths().add(resource.getNode().getPath());
        }
        chain.pushAttribute(request, "resourceAddedInRenderedPath", added);
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("resourceAddedInRenderedPath").equals(true)) {
            renderContext.getRenderedPaths().remove(resource.getNode().getPath());
        }
        return super.execute(previousOut, renderContext, resource, chain);
    }

    public void setConfigurationToSkipInResourceRenderedPath(Set<String> configurationToSkipInResourceRenderedPath) {
        this.configurationToSkipInResourceRenderedPath = configurationToSkipInResourceRenderedPath;
    }
}
