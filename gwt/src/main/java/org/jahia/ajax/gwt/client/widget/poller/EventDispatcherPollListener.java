/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.jahia.ajax.gwt.client.util.EventDataSupplier;
import org.jahia.ajax.gwt.client.util.JsonUtils;
import org.jahia.ajax.gwt.client.widget.poller.Poller.PollListener;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Event dispatcher that listens to broadcasted events (Atmosphere) and dispatches them to listeners, registered via JavaScript callbacks.
 *
 * @author Sergiy Shyrkov
 */
public class EventDispatcherPollListener implements PollListener<RPCEvent> {

    public EventDispatcherPollListener(Class<?>... eventTypes) {
        Poller poller = Poller.getInstance();
        for (Class<?> eventType : eventTypes) {
            poller.registerListener(this, eventType);
        }
    }

    @Override
    public void handlePollingResult(RPCEvent result) {
        if (result instanceof EventDataSupplier && isConsumerRegistered()) {
            dispatchToConsumers(JsonUtils.serialize(((EventDataSupplier) result).getEventData()).getJavaScriptObject());
        }
    }

    private native void dispatchToConsumers(JavaScriptObject eventData) /*-{
        if ($wnd.authoringApi && $wnd.authoringApi.pushEventHandlers && $wnd.authoringApi.pushEventHandlers.length > 0) {
            $wnd.authoringApi.pushEventHandlers.forEach(function(pushEventHandler) {
                pushEventHandler.call(null, eventData);
            });
        }
    }-*/;

    private native boolean isConsumerRegistered() /*-{
        if ($wnd.authoringApi && $wnd.authoringApi.pushEventHandlers && $wnd.authoringApi.pushEventHandlers.length > 0) {
            return true;
        } else {
            return false;
        }
    }-*/;
}
