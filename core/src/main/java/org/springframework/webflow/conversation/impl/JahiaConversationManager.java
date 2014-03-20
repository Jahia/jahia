/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.springframework.webflow.conversation.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.JdkVersion;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JahiaConversationManager extends SessionBindingConversationManager {

    @Override
    protected ConversationContainer createConversationContainer() {
        return new JahiaConversationContainer(getMaxConversations(), getSessionKey());
    }

    class JahiaConversationContainer extends ConversationContainer {
        private final Log logger = LogFactory.getLog(ConversationContainer.class);

        /**
         * Maximum number of conversations in this container. -1 for unlimited.
         */
        private int maxConversations;

        /**
         * The key of this conversation container in the session.
         */
        private String sessionKey;

        /**
         * The contained conversations. A list of {@link ContainedConversation} objects.
         */
        private List conversations;

        /**
         * The sequence for unique conversation identifiers within this container.
         */
        private int conversationIdSequence;

        /**
         * Create a new conversation container.
         * @param maxConversations the maximum number of allowed concurrent conversations, -1 for unlimited
         * @param sessionKey the key of this conversation container in the session
         */
        public JahiaConversationContainer(int maxConversations, String sessionKey) {
            super(maxConversations, sessionKey);
            this.maxConversations = maxConversations;
            this.sessionKey = sessionKey;
            this.conversations = new ArrayList();
        }

        /**
         * Returns the key of this conversation container in the session. For package level use only.
         */
        String getSessionKey() {
            return sessionKey;
        }

        /**
         * Returns the current size of the conversation container: the number of conversations contained within it.
         */
        public int size() {
            return conversations.size();
        }

        /**
         * Create a new conversation based on given parameters and add it to the container.
         * @param parameters descriptive conversation parameters
         * @param lockFactory the lock factory to use to create the conversation lock
         * @return the created conversation
         */
        public synchronized Conversation createConversation(ConversationParameters parameters,
                                                            ConversationLockFactory lockFactory) {
            ContainedConversation conversation = new ContainedConversation(this, nextId(), lockFactory.createLock());
            conversation.putAttribute("name", parameters.getName());
            conversation.putAttribute("caption", parameters.getCaption());
            conversation.putAttribute("description", parameters.getDescription());
            conversations.add(conversation);
            if (maxExceeded()) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("The maximum number of flow executions has been exceeded for the current user. Removing the oldest conversation with id: "
                                    + ((Conversation) conversations.get(0)).getId());
                }
                // end oldest conversation
                ((Conversation) conversations.get(0)).end();
            }
            return conversation;
        }

        private ConversationId nextId() {
            if (JdkVersion.isAtLeastJava15()) {
                return new SimpleConversationId(Integer.valueOf(++conversationIdSequence));
            } else {
                return new SimpleConversationId(new Integer(++conversationIdSequence));
            }
        }

        /**
         * Return the identified conversation.
         * @param id the id to lookup
         * @return the conversation
         * @throws NoSuchConversationException if the conversation cannot be found
         */
        public synchronized Conversation getConversation(ConversationId id) throws NoSuchConversationException {
            for (Iterator it = conversations.iterator(); it.hasNext();) {
                ContainedConversation conversation = (ContainedConversation) it.next();
                if (conversation.getId().equals(id)) {
                    conversations.remove(conversation);
                    conversations.add(conversation);
                    return conversation;
                }
            }
            throw new NoSuchConversationException(id);
        }

        /**
         * Remove identified conversation from this container.
         */
        public synchronized void removeConversation(ConversationId id) {
            for (Iterator it = conversations.iterator(); it.hasNext();) {
                ContainedConversation conversation = (ContainedConversation) it.next();
                if (conversation.getId().equals(id)) {
                    it.remove();
                    break;
                }
            }
        }

        /**
         * Has the maximum number of allowed concurrent conversations in the session been exceeded?
         */
        private boolean maxExceeded() {
            return maxConversations > 0 && conversations.size() > maxConversations;
        }
    }
}
