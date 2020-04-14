/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.id.uuid.UUID;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.ServletRequest;

/**
 * Simple filter that add a unique token and check it when an event is received
 */
public class WebflowTokenListener extends FlowExecutionListenerAdapter {

    public static final String WEBFLOW_TOKEN = "webflowToken";

    @Override
    public void sessionStarting(RequestContext context, FlowSession session, MutableAttributeMap<?> input) {
        String token = UUID.randomUUID().toString();
        context.getFlowScope().put(WEBFLOW_TOKEN, token);
        storeToken(context, token);
        super.sessionStarting(context, session, input);
    }

    @Override
    public void resuming(RequestContext context) {
        String token = (String) context.getFlowScope().get(WEBFLOW_TOKEN);
        storeToken(context, token);
        super.resuming(context);
    }

    private void storeToken(RequestContext context, String token) {
        ((ServletRequest) context.getExternalContext().getNativeRequest()).setAttribute(WEBFLOW_TOKEN, token);
    }

    @Override
    public void eventSignaled(RequestContext context, Event event) {
        String token = (String) context.getFlowScope().get(WEBFLOW_TOKEN);
        String reqToken = context.getRequestParameters().get(WEBFLOW_TOKEN);
        if (token != null && !token.equals(reqToken)) {
            throw new IllegalStateException("Invalid token");
        }

        super.eventSignaled(context, event);
    }
}
