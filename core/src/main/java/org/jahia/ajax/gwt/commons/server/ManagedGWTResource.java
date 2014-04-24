/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

/**
 * simple GWT handler for atmosphere
 */
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.*;
import org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.jahia.api.Constants;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void onReady(final AtmosphereResource r) {
        r.suspend();
        BroadcasterFactory.getDefault().lookup(GWT_BROADCASTER_ID, true).addAtmosphereResource(r);
        JahiaUser user = (JahiaUser) r.getRequest().getSession(true).getAttribute(Constants.SESSION_USER);
        if (user != null) {
            BroadcasterFactory.getDefault().lookup(GWT_BROADCASTER_ID + user.getName(),true).addAtmosphereResource(r);
        }

        logger.debug("Received RPC GET");
    }

    @Disconnect
    public void disconnected(AtmosphereResourceEvent event){
        // isCancelled == true. means the client didn't send the close event, so an unexpected network glitch or browser
        // crash occurred.
        if (event.isCancelled()) {
            logger.info("User:" + event.getResource().uuid() + " unexpectedly disconnected");
        } else if (event.isClosedByClient()) {
            if (logger.isDebugEnabled()) {
                logger.debug("User:" + event.getResource().uuid() + " closed the connection");
            }
        }
    }

    @Post
    public void post(AtmosphereResource r) {
        // Don't need to do anything, the interceptor took care of it for us.
        if (logger.isDebugEnabled()) {
            logger.info("POST received with transport {}", r.transport());
        }
    }
}
