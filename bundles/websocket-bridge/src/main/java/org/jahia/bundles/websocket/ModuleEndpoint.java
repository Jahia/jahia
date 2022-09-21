/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.websocket;

import org.osgi.framework.ServiceObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is the endpoint wrapper to wrap the module endpoint instance.
 */
public class ModuleEndpoint extends Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(ModuleEndpoint.class);
    private final ServiceObjects<Endpoint> endpointServiceRef;
    private final Endpoint endpoint;
    private final Set<Session> sessions = new HashSet<>();
    private volatile boolean closed;

    public ModuleEndpoint(ServiceObjects<Endpoint> endpointServiceRef) {
        this.endpointServiceRef = endpointServiceRef;
        this.endpoint = endpointServiceRef.getService();
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (closed) {
            return;
        }

        endpoint.onClose(session, closeReason);
        sessions.remove(session);
        endpointServiceRef.ungetService(endpoint);
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        if (closed) {
            return;
        }

        endpoint.onError(session, throwable);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        if (closed) {
            return;
        }

        endpoint.onOpen(session, endpointConfig);
        sessions.add(session);
    }

    protected void close() {
        closed = true;

        Iterator<Session> iterator = sessions.iterator();

        while (iterator.hasNext()) {
            Session session = iterator.next();
            iterator.remove();

            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Service is unregistered");
                session.close(closeReason);
                endpoint.onClose(session, closeReason);
                endpointServiceRef.ungetService(endpoint);
            } catch (IOException e) {
                logger.error("Unable to close session", e);
            }
        }
    }
}