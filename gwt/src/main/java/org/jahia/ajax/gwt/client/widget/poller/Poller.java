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
package org.jahia.ajax.gwt.client.widget.poller;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import org.atmosphere.gwt20.client.Atmosphere;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
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
    private static final int RECONNECT_INTERVAL_MS = 2000;
    private static final int CONNECTION_RESTORED_NOTIFICATION_DELAY_MS = 15000;
    private static Poller instance;

    static {
        String userAgent = GXT.getUserAgent();
        SSE_SUPPORT = !(GXT.isIE || userAgent != null && userAgent.indexOf("trident/7") != -1);
    }

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

    public Poller(final boolean useWebsockets) {
        RPCSerializer serializer = GWT.create(RPCSerializer.class);

        if (initGWTMessageHandler(this, serializer)) {
            Scheduler.get().scheduleDeferred(() -> {
                AtmosphereRequestConfig requestConfig = AtmosphereRequestConfig.create(serializer);
                requestConfig.setUrl(GWT.getModuleBaseURL().substring(0, GWT.getModuleBaseURL().indexOf("/gwt/")) + "/atmosphere/rpc?windowId=" + JahiaContentManagementService.App.getWindowId());
                Transport transport = useWebsockets ? AtmosphereRequestConfig.Transport.WEBSOCKET : (SSE_SUPPORT ? AtmosphereRequestConfig.Transport.SSE : AtmosphereRequestConfig.Transport.STREAMING);
                requestConfig.setTransport(transport);
                requestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
                requestConfig.setMaxReconnectOnClose(Integer.MAX_VALUE);
                requestConfig.setReconnectInterval(RECONNECT_INTERVAL_MS);

                requestConfig.setErrorHandler(response -> {
                    // This only happens on unexpected communication failures, not on conventional connection close.
                    GWT.log("RPC error");
                    reconnectingAfterError = true;
                });

                requestConfig.setReconnectHandler((request, response) -> GWT.log("RPC reconnection"));

                requestConfig.setOpenHandler(response -> {
                    GWT.log("RPC Connection opened");
                    onConnectionOpen();
                });

                requestConfig.setReopenHandler(response -> {
                    GWT.log("RPC Connection reopened");
                    PermissionsUtils.triggerPermissionsReload();
                    onConnectionOpen();
                });

                requestConfig.setCloseHandler(response -> GWT.log("RPC Connection closed"));

                requestConfig.setMessageHandler(response -> {
                    List<RPCEvent> messages = response.getMessages();
                    handleMessages(messages);
                    dispatchToGWTConsumers(response.getResponseBody());
                });

                requestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
                requestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);

                Atmosphere atmosphere = Atmosphere.create();
                atmosphere.subscribe(requestConfig);
            });
        }
    }

    public static Poller getInstance() {
        if (instance == null) {
            instance = new Poller(JahiaGWTParameters.isWebSockets());
        }
        return instance;
    }

    private void handleMessages(List<RPCEvent> messages) {
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

    private native boolean initGWTMessageHandler(Poller that, RPCSerializer serializer) /*-{
        if ($wnd.parent !== $wnd && $wnd.parent.authoringApi) {
            $wnd.parent.authoringApi.gwtMessageHandler = function (event) {
                that.@Poller::handleMessages(*)(serializer.@RPCSerializer::deserialize(*)(event));
            };
            $wnd.addEventListener("unload", function() {
                $wnd.parent.authoringApi.gwtMessageHandler = null;
            });

            return false;
        } else {
            $wnd.authoringApi = $wnd.authoringApi || {};
            $wnd.authoringApi.gwtMessageHandler = null;
        }
        return true;
    }-*/;

    private native void dispatchToGWTConsumers(String eventData) /*-{
        if ($wnd.authoringApi && $wnd.authoringApi.gwtMessageHandler) {
            $wnd.authoringApi.gwtMessageHandler(eventData);
        }
    }-*/;

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
            listeners.put(eventType, new ArrayList<>());
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
