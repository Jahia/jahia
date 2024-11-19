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
package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.filter.TemplateAttributesFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Render action that executes the Webflow request processing.
 */
public class WebflowAction extends Action {

    private RenderService renderService;

    static final String WEBFLOW_LOCALE_PARAMETER = "webflowLocale";

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String forcedLocale = null;
        if (parameters != null && parameters.containsKey(WEBFLOW_LOCALE_PARAMETER)) {
            forcedLocale = parameters.remove(WEBFLOW_LOCALE_PARAMETER).get(0);
        }
        Enumeration<?> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            if (parameterName.startsWith("webflowexecution")) {
                String id = parameterName.substring("webflowexecution".length()).replace('_', '-');
                String view = "default";
                if (id.contains("--")) {
                    view = StringUtils.substringAfter(id, "--");
                    id = StringUtils.substringBefore(id, "--");
                }
                if (forcedLocale != null) {
                    req.setAttribute(TemplateAttributesFilter.FORCED_LOCALE_ATTRIBUTE, Locale.forLanguageTag(forcedLocale));
                }
                JCRNodeWrapper node = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession(renderContext.getWorkspace(), renderContext.getMainResourceLocale()).getNodeByUUID(id);
                req.setAttribute("actionParameters", parameters);
                renderService.render(new Resource(node, urlResolver.getResource().getTemplateType(), view, Resource.CONFIGURATION_MODULE), renderContext);
                return new ActionResult(HttpServletResponse.SC_OK, renderContext.getRedirect(), true, null);
            }
        }
        return ActionResult.OK;
    }
}
