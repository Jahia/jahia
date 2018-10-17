/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
        if ($wnd.authoringApi && $wnd.authoringApi.pushEventConsumers && $wnd.authoringApi.pushEventConsumers.length > 0) {
            $wnd.authoringApi.pushEventConsumers.forEach(function(consumer) {
                consumer.call(null, eventData);
            });
        }
    }-*/;

    private native boolean isConsumerRegistered() /*-{
        if ($wnd.authoringApi && $wnd.authoringApi.pushEventConsumers && $wnd.authoringApi.pushEventConsumers.length > 0) {
            return true;
        } else {
            return false;
        }
    }-*/;
}
