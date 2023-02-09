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
    public static final String CHECK_WEBFLOW_TOKEN = "checkWebflowToken";

    @Override
    public void sessionStarting(RequestContext context, FlowSession session, MutableAttributeMap<?> input) {
        String token = UUID.randomUUID().toString();
        context.getFlowScope().put(WEBFLOW_TOKEN, token);
        storeToken(context, token);
        super.sessionStarting(context, session, input);
    }

    @Override
    public void resuming(RequestContext context) {
        context.getFlowScope().put(CHECK_WEBFLOW_TOKEN, true);
        String token = (String) context.getFlowScope().get(WEBFLOW_TOKEN);
        storeToken(context, token);
        super.resuming(context);
    }

    private void storeToken(RequestContext context, String token) {
        ((ServletRequest) context.getExternalContext().getNativeRequest()).setAttribute(WEBFLOW_TOKEN, token);
    }

    @Override
    public void eventSignaled(RequestContext context, Event event) {
        if (context.getFlowScope().get(CHECK_WEBFLOW_TOKEN) != null) {
            String token = (String) context.getFlowScope().get(WEBFLOW_TOKEN);
            String reqToken = context.getRequestParameters().get(WEBFLOW_TOKEN);
            if (token != null && !token.equals(reqToken)) {
                throw new IllegalStateException("Invalid token");
            }
        }

        super.eventSignaled(context, event);
    }
}
