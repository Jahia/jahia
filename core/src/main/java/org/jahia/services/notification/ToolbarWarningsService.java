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
package org.jahia.services.notification;

import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.ajax.gwt.client.widget.poller.ToolbarWarningEvent;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.services.atmosphere.AtmosphereServlet;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import java.util.*;

/**
 * Simple service used to fill potential warnings during startup/execution
 * return them as translated messages.
 */
public class ToolbarWarningsService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ToolbarWarningsService.class);

    private class Message {

        /**
         * Initializes an instance of this class.
         *
         * @param key
         *            the resource bundle key
         * @param args
         *            message arguments
         */
        Message(String key, Object[] args) {
            super();
            this.key = key;
            this.args = args;
        }

        private String key;

        private Object[] args;
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ToolbarWarningsService INSTANCE = new ToolbarWarningsService();
    }

    public static ToolbarWarningsService getInstance() {
        return Holder.INSTANCE;
    }

    private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());

    /**
     * Add a message as a resource key
     * @param message
     */
    public void addMessage(String message) {
        addMessage(false, message);
    }

    /**
     * Add a message as a resource key to the list of messages.
     *
     * @param message
     *            the message resource key to be added
     *
     * @param atTheTop
     *            <code>true</code> if the messages should be put at the top of the message list; otherwise it is appended to the end
     */
    public void addMessage(boolean atTheTop, String message, Object... args) {
        removeMessage(message);
        Message m = new Message(message, args);
        if (atTheTop) {
            messages.add(0, m);
            broadcastMessagesUpdate();
        } else {
            messages.add(m);
            broadcastMessagesUpdate();
        }
    }

    /**
     * Removes the specified message from the messages list.
     *
     * @param message
     *            the message to be removed
     */
    public void removeMessage(String message) {
        if (messages != null) {
            for (Iterator<Message> it = messages.iterator(); it.hasNext();) {
                if (it.next().key.equals(message)) {
                    it.remove();
                    broadcastMessagesUpdate();
                }
            }
        }
    }

    private void broadcastMessagesUpdate() {
        final BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
        if(broadcasterFactory != null) {
            Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID);
            if (broadcaster != null) {
                broadcaster.broadcast(new ToolbarWarningEvent());
            } else {
                logger.debug("Fail to broadcast Toolbar warning message event update, because broadcaster not found");
            }
        } else {
            logger.debug("Fail to broadcast Toolbar warning message event update, because broadcaster factory not ready");
        }
    }

    public String getMessagesAsString() {
        return messages != null ? StringUtils.join(messages, "||") : "";
    }

    /**
     * Return resources messages as a String using || as separator.
     * @param locale the locale to be used to translate the resource bundle messages into localized ones
     * @return messages joined
     */
    public String getMessagesValueAsString(Locale locale) {
        if (messages == null) {
            return "";
        }
        List<String> translatedMessages = new ArrayList<String>();
        for (Message m : messages) {
            translatedMessages.add(m.args != null ? Messages.getInternalWithArguments(m.key, m.key, locale, m.args)
                    : Messages.getInternal(m.key, locale, m.key));
        }
        return StringUtils.join(translatedMessages, "||");
    }

}
