/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
 * @author rincevent
 */
class BundleFlowViewResolver implements FlowViewResolver {
    @Override
    public View resolveView(String viewId, RequestContext context) {
        if (viewId.startsWith("/")) {
            return getViewInternal(viewId, context, context.getActiveFlow().getApplicationContext());
        } else {
            ApplicationContext flowContext = context.getActiveFlow().getApplicationContext();
            if (flowContext == null) {
                throw new IllegalStateException(
                        "A Flow ApplicationContext is required to resolve Flow View Resources");
            }
            Resource viewResource = flowContext.getResource(viewId);
            if (!(viewResource instanceof ContextResource)) {
                throw new IllegalStateException(
                        "A ContextResource is required to get relative view paths within this context");
            }
            return getViewInternal(((ContextResource) viewResource).getPathWithinContext(), context, flowContext);
        }
    }

    @Override
    public String getViewIdByConvention(String viewStateId) {
        return viewStateId + ".jsp";
    }

    private View getViewInternal(String viewPath, RequestContext context, ApplicationContext flowContext) {
        if (viewPath.endsWith(".jsp") || viewPath.endsWith(".jspx")) {
            InternalResourceView view = new InternalResourceView(viewPath);
            view.setApplicationContext(flowContext);
            return view;
        } else {
            throw new IllegalArgumentException("Unsupported view type " + viewPath +
                                               " only types supported by this FlowViewResolver implementation are [.jsp] and [.jspx]");
        }
    }
}
