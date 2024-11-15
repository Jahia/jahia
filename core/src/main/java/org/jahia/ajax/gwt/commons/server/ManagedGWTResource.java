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
package org.jahia.ajax.gwt.commons.server;

import org.apache.commons.lang.StringUtils;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpSession;

/**
 * simple GWT handler for atmosphere
 */

@ManagedService(path = "/atmosphere/rpc",
        interceptors = {
                /**
                 * Handle lifecycle for us
                 */
                AtmosphereResourceLifecycleInterceptor.class,
                /**
                 * Send to the client the size of the message to prevent serialization error.
                 */
                TrackMessageSizeInterceptor.class,
                /**
                 * Serialize/Deserialize GWT message for us
                 */
                GwtRpcInterceptor.class,
                /**
                 * Make sure our {@link AtmosphereResourceEventListener#onSuspend} is only called once for transport
                 * that reconnect on every requests.
                 */
                SuspendTrackerInterceptor.class,
                /**
                 * Deserialize the GWT message
                 */
                AtmosphereMessageInterceptor.class,
                /**
                 * Echo the messages we are receiving from the client either as w WebSocket message or an HTTP Post.
                 */
                BroadcastOnPostAtmosphereInterceptor.class,
                /**
                 * Discard idle AtmosphereResource in case the network didn't advise us the client disconnected
                 */
                IdleResourceInterceptor.class
        })
public class ManagedGWTResource {

    static final Logger logger = LoggerFactory.getLogger(ManagedGWTResource.class);

    public static final String GWT_BROADCASTER_ID = "org.jahia.broadcaster";

    @Ready
    public String onReady(final AtmosphereResource r) {
        r.suspend();
        BroadcasterFactory broadcasterFactory = r.getAtmosphereConfig().getBroadcasterFactory();
        broadcasterFactory.lookup(GWT_BROADCASTER_ID, true).addAtmosphereResource(r);
        JahiaUser user = (JahiaUser) r.getRequest().getSession(true).getAttribute(Constants.SESSION_USER);
        if (user != null) {
            broadcasterFactory.lookup(GWT_BROADCASTER_ID + user.getName(), true).addAtmosphereResource(r);
        }

        SpringContextSingleton.getInstance().publishEvent(new AtmosphereClientReadyEvent(r));
        logger.debug("Received RPC GET");
        // Work around to avoid NPE at initialisation
        return StringUtils.EMPTY;
    }

    @Disconnect
    public void disconnected(AtmosphereResourceEvent event) {
        // isCancelled == true. means the client didn't send the close event, so an unexpected network glitch or browser
        // crash occurred.
        if (event.isCancelled()) {
            HttpSession session = event.getResource().getRequest().getSession();
            JahiaUser user = (JahiaUser) session.getAttribute(Constants.SESSION_USER);
            String userName = user != null ? user.getUsername() : null;
            logger.info("User's AtmosphereResource unexpectedly disconnected! user=[{}] session=[{}]", userName,
                    session.getId());
        } else if (event.isClosedByClient() && logger.isDebugEnabled()) {
            HttpSession session = event.getResource().getRequest().getSession();
            JahiaUser user = (JahiaUser) session.getAttribute(Constants.SESSION_USER);
            logger.debug("User closed the connection for AtmosphereResource! user=[{}] session=[{}]",
                    user != null ? user.getUsername() : null, session.getId());
        }
        SpringContextSingleton.getInstance().publishEvent(new AtmosphereClientDisconnectedEvent(event.getResource()));
    }

    @Post
    public void post(AtmosphereResource r) {
        // Don't need to do anything, the interceptor took care of it for us.
        if (logger.isDebugEnabled()) {
            logger.info("POST received with transport {}", r.transport());
        }
    }

    public class AtmosphereClientReadyEvent extends ApplicationEvent {
        private static final long serialVersionUID = 1L;

        public AtmosphereClientReadyEvent(AtmosphereResource resource) {
            super(resource);
        }

        public AtmosphereResource getResource() {
            return (AtmosphereResource) super.getSource();
        }
    }

    public class AtmosphereClientDisconnectedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 1L;

        public AtmosphereClientDisconnectedEvent(AtmosphereResource resource) {
            super(resource);
        }

        public AtmosphereResource getResource() {
            return (AtmosphereResource) super.getSource();
        }
    }
}
