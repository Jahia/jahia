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
package org.jahia.services.atmosphere;

import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.websocket.WebSocketEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsLogger implements WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EventsLogger.class);

    public EventsLogger() {
    }

    public void onSuspend(final AtmosphereResourceEvent event) {
        logger.info("onSuspend(): {}:{}", event.getResource().getRequest().getRemoteAddr(),
                event.getResource().getRequest().getRemotePort());
    }

    public void onResume(AtmosphereResourceEvent event) {
        logger.info("onResume(): {}:{}", event.getResource().getRequest().getRemoteAddr(),
                event.getResource().getRequest().getRemotePort());
    }

    public void onDisconnect(AtmosphereResourceEvent event) {
        logger.info("onDisconnect(): {}:{}", event.getResource().getRequest().getRemoteAddr(),
                event.getResource().getRequest().getRemotePort());
    }

    public void onBroadcast(AtmosphereResourceEvent event) {
        logger.info("onBroadcast(): {}", event.getMessage());
    }

    public void onThrowable(AtmosphereResourceEvent event) {
        logger.warn("onThrowable(): {}", event);
    }

    public void onHandshake(WebSocketEvent event) {
        logger.info("onHandshake(): {}", event);
    }

    public void onMessage(WebSocketEvent event) {
        logger.info("onMessage(): {}", event);
    }

    public void onClose(WebSocketEvent event) {
        logger.info("onClose(): {}", event);
    }

    public void onControl(WebSocketEvent event) {
        logger.info("onControl(): {}", event);
    }

    public void onDisconnect(WebSocketEvent event) {
        logger.info("onDisconnect(): {}", event);
    }

    public void onConnect(WebSocketEvent event) {
        logger.info("onConnect(): {}", event);
    }

    public void onPreSuspend(AtmosphereResourceEvent event) {
        logger.info("onPreSuspend(): {}", event);
    }

    public void onClose(AtmosphereResourceEvent event) {
        logger.info("onClose(): {}", event);
    }

    @Override
    public void onHeartbeat(AtmosphereResourceEvent event) {
        logger.info("onHeartbeat(): {}", event);
    }
}
