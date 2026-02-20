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

        String contextPath;
        if (context.isEditMode()) {
            contextPath = StringUtils.substringAfterLast(((EditConfiguration) SpringContextSingleton.getBean(context.getEditModeConfigName())).getDefaultUrlMapping(), "/");
        } else {
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
