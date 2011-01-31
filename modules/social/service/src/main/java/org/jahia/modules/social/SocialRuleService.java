/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.social;

import java.util.List;

import javax.jcr.RepositoryException;

import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.rules.AddedNodeFact;

/**
 * Social service class for manipulating social activities from the
 * right-hand-side (consequences) of rules.
 *
 * @author Serge Huber
 */
public class SocialRuleService {
    
    private SocialService socialService;

    /* Rules Consequence implementations */

    public void addActivity(final String activityType, final String user, final String messageKey, final AddedNodeFact nodeFact, final List<String> nodeTypeList, KnowledgeHelper drools) throws RepositoryException {
        socialService.addActivity(activityType, user, null, messageKey, nodeFact.getNode(), nodeTypeList, nodeFact.getNode().getSession());
    }

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, AddedNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        socialService.sendMessage(fromUser, toUser, subject, message, nodeFact.getNode().getSession());
    }

    /**
     * @param socialService the socialService to set
     */
    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

}
