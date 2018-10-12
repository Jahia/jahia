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
import org.jahia.ajax.gwt.client.util.JsonSerializable;
import org.jahia.ajax.gwt.client.util.JsonUtils;
import org.jahia.ajax.gwt.client.widget.poller.Poller.PollListener;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Event dispatcher that listens on broadcasted events (Atmosphere) and dispatches them to listeners, registered via JavaScript callbacks.
 * 
 * @author Sergiy Shyrkov
 */
public class EventDispatcherPollListener implements PollListener<RPCEvent> {

    public static void register() {
        PollListener<RPCEvent> listener = new EventDispatcherPollListener();
        Poller poller = Poller.getInstance();
        poller.registerListener(listener, TaskEvent.class);
        poller.registerListener(listener, ProcessPollingEvent.class);
    }

    @Override
    public void handlePollingResult(RPCEvent result) {
        if (result instanceof JsonSerializable && isConsumerRegistered()) {
            dispatchToConsumers(JsonUtils.serialize(((JsonSerializable) result).getDataForJsonSerialization()).getJavaScriptObject());
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
