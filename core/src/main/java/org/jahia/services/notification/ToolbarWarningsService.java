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
