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
package org.springframework.webflow.conversation.impl;

import java.util.Iterator;

import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.NoSuchConversationException;

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
