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
package org.jahia.ajax.gwt.client.widget.poller;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;

import org.atmosphere.gwt20.client.*;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.atmosphere.gwt20.client.managed.RPCSerializer;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execute recurrent calls to the server
 */
public class Poller {

    private static final boolean SSE_SUPPORT;
    static {
        String userAgent = GXT.getUserAgent();
        SSE_SUPPORT = !(GXT.isIE || userAgent != null && userAgent.indexOf("trident/7") != -1);
    }

    private static final int RECONNECT_INTERVAL_MS = 2000;
    private static final int CONNECTION_RESTORED_NOTIFICATION_DELAY_MS = 15000;

    private static Poller instance;

    private Map<Class, ArrayList<PollListener>> listeners = new HashMap<Class, ArrayList<PollListener>>();

    // There are two basic scenarios of how instant communication with the server may be broken:
    // 1) The server closes the connection in a conventional way (for example, due to shutdown).
    // 2) The server disappears unexpectedly (for example, due to a network issue).
    // Atmosphere in cooperation with the browser is able to restore the connection and continue retrieving messages in both cases when the server is available again,
    // however in case 2) there might be messages broadcasted while the client was disconnected, which are typically lost. In order to mitigate this lost messages issue,
    // we track the error/reconnection status and suggest page reloading to the user when the connection is restored. In this way, client side state gets synchronized
    // with server side even though there were some missing messages. However, note that we rely on corresponding Atmosphere events when tracking the connection state,
    // so cannot actually track it precisely, since, for example, Atmosphere in cooperation with browser, OS, device driver, hardware or whatever is involved, themselves
    // often need few seconds to tens seconds to recognize connection status change.
    private boolean reconnectingAfterError;

    public static Poller getInstance() {
        if (instance == null) {
            instance = new Poller(JahiaGWTParameters.isWebSockets());
        }
        return instance;
    }

    public Poller(final boolean useWebsockets) {

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

            @Override
            public void execute() {

                RPCSerializer serializer = GWT.create(RPCSerializer.class);
                AtmosphereRequestConfig requestConfig = AtmosphereRequestConfig.create(serializer);
                requestConfig.setUrl(GWT.getModuleBaseURL().substring(0, GWT.getModuleBaseURL().indexOf("/gwt/")) + "/atmosphere/rpc?windowId=" + JahiaContentManagementService.App.getWindowId());
                Transport transport = useWebsockets ? AtmosphereRequestConfig.Transport.WEBSOCKET : (SSE_SUPPORT ? AtmosphereRequestConfig.Transport.SSE : AtmosphereRequestConfig.Transport.STREAMING);
                requestConfig.setTransport(transport);
                requestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
                requestConfig.setMaxReconnectOnClose(Integer.MAX_VALUE);
                requestConfig.setReconnectInterval(RECONNECT_INTERVAL_MS);

                requestConfig.setErrorHandler(new AtmosphereErrorHandler() {

                    @Override
                    public void onError(AtmosphereResponse response) {
                        // This only happens on unexpected communication failures, not on conventional connection close.
                        GWT.log("RPC error");
                        reconnectingAfterError = true;
                    }
                });

                requestConfig.setReconnectHandler(new AtmosphereReconnectHandler() {

                    @Override
                    public void onReconnect(RequestConfig request, AtmosphereResponse response) {
                        GWT.log("RPC reconnection");
                    }
                });

                requestConfig.setOpenHandler(new AtmosphereOpenHandler() {

                    @Override
                    public void onOpen(AtmosphereResponse response) {
                        GWT.log("RPC Connection opened");
                        onConnectionOpen();
                    }
                });

                requestConfig.setReopenHandler(new AtmosphereReopenHandler() {

                    @Override
                    public void onReopen(AtmosphereResponse response) {
                        GWT.log("RPC Connection reopened");
                        PermissionsUtils.triggerPermissionsReload();
                        onConnectionOpen();
                    }
                });

                requestConfig.setCloseHandler(new AtmosphereCloseHandler() {

                    @Override
                    public void onClose(AtmosphereResponse response) {
                        GWT.log("RPC Connection closed");
                    }
                });

                requestConfig.setMessageHandler(new AtmosphereMessageHandler() {

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

                requestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
                requestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);

                Atmosphere atmosphere = Atmosphere.create();
                atmosphere.subscribe(requestConfig);
            }
        });
    }

    private void onConnectionOpen() {
        if (!reconnectingAfterError) {
            // Suggest page reloading to the user only in case there was an unexpected communication failure, so some messages might be lost.
            return;
        }
        reconnectingAfterError = false;

        InfoConfig infoConfig = new InfoConfig(Messages.get("label.information"), Messages.get("instantMessaging.connectionRecovered.notification"));
        infoConfig.display = CONNECTION_RESTORED_NOTIFICATION_DELAY_MS;
        Info.display(infoConfig);
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
