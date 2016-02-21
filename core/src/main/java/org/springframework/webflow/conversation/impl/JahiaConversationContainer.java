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
package org.springframework.webflow.conversation.impl;

import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.NoSuchConversationException;

import java.util.Iterator;

/**
 * Container for Web Flow conversations that is stored in the HTTP session. 
 */
class JahiaConversationContainer extends ConversationContainer {
    private static final long serialVersionUID = 6043504320583152706L;

    /**
     * Create a new conversation container.
     * @param maxConversations the maximum number of allowed concurrent conversations, -1 for unlimited
     * @param sessionKey the key of this conversation container in the session
     */
    public JahiaConversationContainer(int maxConversations, String sessionKey) {
        super(maxConversations, sessionKey);
    }

    /**
     * Return the identified conversation.
     * @param id the id to lookup
     * @return the conversation
     * @throws NoSuchConversationException if the conversation cannot be found
     */
    @Override
    public synchronized Conversation getConversation(ConversationId id) throws NoSuchConversationException {
        for (Iterator<ContainedConversation> it = getConversations().iterator(); it.hasNext();) {
            ContainedConversation conversation = (ContainedConversation) it.next();
            if (conversation.getId().equals(id)) {
                getConversations().remove(conversation);
                getConversations().add(conversation);
                return conversation;
            }
        }
        throw new NoSuchConversationException(id);
    }
}