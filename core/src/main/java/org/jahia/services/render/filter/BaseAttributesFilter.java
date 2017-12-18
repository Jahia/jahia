/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Set;

/**
 * Stores the required request parameters before evaluating the template and restores original after.
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class BaseAttributesFilter extends AbstractFilter {

    public Set<String> configurationToSkipInResourceRenderedPath;

    @Override
    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {

        final HttpServletRequest request = context.getRequest();

        request.setAttribute("renderContext", context);
        chain.pushAttribute(request, "currentResource", resource);

        String contextPath = null;
        if(context.isEditMode() && ! context.isContributionMode()){
            contextPath = StringUtils.substringAfterLast(((EditConfiguration) SpringContextSingleton.getBean(context.getEditModeConfigName())).getDefaultUrlMapping(), "/");
        } else if(context.isContributionMode()){
            contextPath = "contribute";
        } else{
            contextPath = "render";
        }
        String mode = contextPath + "/" + resource.getWorkspace();

        chain.pushAttribute(request, "currentLocale", resource.getLocale());
        chain.pushAttribute(request, "currentMode", mode);
        chain.pushAttribute(request, "currentUser", context.getMainResource().getNode().getSession().getUser());
        chain.pushAttribute(request, "currentAliasUser", context.getMainResource().getNode().getSession().getAliasedUser());
        if (!Resource.CONFIGURATION_INCLUDE.equals(resource.getContextConfiguration())) {
            chain.pushAttribute(request, "url", new URLGenerator(context, resource));
        }
        boolean added = false;
        if(!configurationToSkipInResourceRenderedPath.contains(resource.getContextConfiguration())) {
            added = context.getRenderedPaths().add(resource.getNodePath());
        }
        chain.pushAttribute(request, "resourceAddedInRenderedPath", added);

        if (!resource.getContextConfiguration().equals(Resource.CONFIGURATION_INCLUDE)) {
            chain.pushAttribute(request, "moduleMap", new HashMap());
        }

        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("resourceAddedInRenderedPath").equals(true)) {
            renderContext.getRenderedPaths().remove(resource.getNodePath());
        }
        return super.execute(previousOut, renderContext, resource, chain);
    }

    public void setConfigurationToSkipInResourceRenderedPath(Set<String> configurationToSkipInResourceRenderedPath) {
        this.configurationToSkipInResourceRenderedPath = configurationToSkipInResourceRenderedPath;
    }
}
