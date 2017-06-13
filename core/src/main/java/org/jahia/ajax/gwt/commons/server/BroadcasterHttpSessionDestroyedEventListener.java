/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.commons.server;

import static org.jahia.ajax.gwt.commons.server.ManagedGWTResource.GWT_BROADCASTER_ID;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener.HttpSessionDestroyedEvent;
import org.jahia.services.atmosphere.AtmosphereServlet;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

/**
 * Application event listener for the {@link HttpSessionDestroyedEvent} that unregisters the Atmosphere broadcaster for a user.
 * 
 * @author Sergiy Shyrkov
 */
public class BroadcasterHttpSessionDestroyedEventListener implements ApplicationListener<HttpSessionDestroyedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BroadcasterHttpSessionDestroyedEventListener.class);

    private boolean hasUserSpecificBroadcasters(Collection<Broadcaster> broadcasters) {
        for (Broadcaster b : broadcasters) {
            String id = b.getID();
            if (id.length() > GWT_BROADCASTER_ID.length() && id.startsWith(GWT_BROADCASTER_ID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onApplicationEvent(HttpSessionDestroyedEvent event) {
        HttpSession httpSession = event.getSession();
        JahiaUser user = null;
        try {
            user = (JahiaUser) httpSession.getAttribute(Constants.SESSION_USER);
        } catch (IllegalStateException e) {
            // the session is already invalidated
        }
        if (user != null) {
            String userName = user.getName();
            BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
            if (broadcasterFactory != null) {
                if (broadcasterFactory.remove(GWT_BROADCASTER_ID + userName)) {
                    logger.debug("Atmosphere broadcaster successfully unregistered for user: {}", userName);
                }
                if (!hasUserSpecificBroadcasters(broadcasterFactory.lookupAll())
                        && broadcasterFactory.remove(GWT_BROADCASTER_ID)) {
                    logger.debug("Atmosphere broadcaster successfully unregistered");
                }
            }
        }
    }

}
