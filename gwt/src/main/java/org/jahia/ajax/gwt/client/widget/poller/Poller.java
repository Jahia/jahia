/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
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
 *
 */
package org.jahia.ajax.gwt.client.widget.poller;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;

import org.atmosphere.gwt20.client.*;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.atmosphere.gwt20.client.managed.RPCSerializer;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execute recurrent calls to the server
 */
public class Poller {

    private static final boolean isIE;

    private static Poller instance;
    
    static {
        String ua = GXT.getUserAgent();
        isIE = GXT.isIE || ua != null && ua.indexOf("trident/7") != -1; 
    }

    private Map<Class, ArrayList<PollListener>> listeners = new HashMap<Class, ArrayList<PollListener>>();

    public static Poller getInstance() {
        if (instance == null) {
            instance = new Poller(JahiaGWTParameters.isWebSockets());
        }
        return instance;
    }

    public Poller(final boolean useWebsockets) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {

                RPCSerializer rpc_serializer = GWT.create(RPCSerializer.class);

                AtmosphereRequestConfig rpcRequestConfig = AtmosphereRequestConfig.create(rpc_serializer);
                rpcRequestConfig.setUrl(GWT.getModuleBaseURL() .substring(0,GWT.getModuleBaseURL() .indexOf("/gwt/")) + "/atmosphere/rpc?windowId="+ JahiaContentManagementService.App.getWindowId());
                Transport transport = useWebsockets ? AtmosphereRequestConfig.Transport.WEBSOCKET
                        : (!isIE ? AtmosphereRequestConfig.Transport.SSE
                                : AtmosphereRequestConfig.Transport.STREAMING);
                rpcRequestConfig.setTransport(transport);
                rpcRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
                rpcRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
                    @Override
                    public void onOpen(AtmosphereResponse response) {
                        GWT.log("RPC Connection opened");
                    }
                });
                rpcRequestConfig.setReopenHandler(new AtmosphereReopenHandler() {
                    @Override
                    public void onReopen(AtmosphereResponse response) {
                        GWT.log("RPC Connection reopened");
                    }
                });
                rpcRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
                    @Override
                    public void onClose(AtmosphereResponse response) {
                        GWT.log("RPC Connection closed");
                    }
                });
                rpcRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
                    @Override
                    public void onMessage(AtmosphereResponse response) {
                        List<RPCEvent> messages = response.getMessages();
                        for (RPCEvent event : messages) {
                            for (Map.Entry<Class, ArrayList<PollListener>> entry : listeners.entrySet()) {
                                if (entry.getKey() == event.getClass()) {
                                    for (PollListener pollListener : entry.getValue()) {
                                        pollListener.handlePollingResult(event);
                                    }
                                }
                            }
                        }
                    }
                });

                rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
                rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);

                // init atmosphere
                Atmosphere atmosphere = Atmosphere.create();
                atmosphere.subscribe(rpcRequestConfig);
            }
        });
    }

    public void registerListener(PollListener listener, Class eventType) {
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new ArrayList<PollListener>());
        }
        listeners.get(eventType).add(listener);
    }

    public void unregisterListener(PollListener listener, Class eventType) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
        }
    }

    public interface PollListener<T> {
        public void handlePollingResult(T result);
    }


}
