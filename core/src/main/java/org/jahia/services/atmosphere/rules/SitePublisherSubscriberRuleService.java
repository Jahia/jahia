/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.atmosphere.rules;

import org.drools.core.spi.KnowledgeHelper;
import org.jahia.services.atmosphere.service.PublisherSubscriberService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.AddedNodeFact;

/**
 * User: rincevent
 * Date: 22/11/11
 * Time: 10:10 AM
 */
public class SitePublisherSubscriberRuleService {
    private PublisherSubscriberService publisherSubscriberService;

    public void setPublisherSubscriberService(PublisherSubscriberService publisherSubscriberService) {
        this.publisherSubscriberService = publisherSubscriberService;
    }

    public void sendSiteMessage(AddedNodeFact nodeFact, Object[] params, KnowledgeHelper drools) {
        publisherSubscriberService.publishToSite(nodeFact.getNode(), params[0].toString());
    }

    public void sendAbsoluteMessage(String absolute, Object[] params, KnowledgeHelper drools) {
        publisherSubscriberService.publishToAbsoluteChannel(absolute, params[0].toString());
    }

    public void sendNodeMessage(AddedNodeFact nodeFact, Object[] params, KnowledgeHelper drools) {
        JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(nodeFact.getNode(), "jnt:page");
        if (parentOfType != null) {
            publisherSubscriberService.publishToNodeChannel(parentOfType, params[0].toString());
        }
    }
}
