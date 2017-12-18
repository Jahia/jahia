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
