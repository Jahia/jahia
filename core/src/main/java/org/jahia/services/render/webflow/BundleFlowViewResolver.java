/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.webflow;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
* Created by IntelliJ IDEA.
*
* @author : rincevent
* @since : JAHIA 6.1
*        Created : 08/03/13
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
