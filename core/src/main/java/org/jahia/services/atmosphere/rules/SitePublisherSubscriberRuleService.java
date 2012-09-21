package org.jahia.services.atmosphere.rules;

import org.drools.spi.KnowledgeHelper;
import org.jahia.services.atmosphere.service.PublisherSubscriberService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.AddedNodeFact;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 22/11/11
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class SitePublisherSubscriberRuleService {
    private PublisherSubscriberService publisherSubscriberService;

    public void setPublisherSubscriberService(PublisherSubscriberService publisherSubscriberService) {
        this.publisherSubscriberService = publisherSubscriberService;
    }

    public void sendSiteMessage(AddedNodeFact nodeFact, String message, KnowledgeHelper drools) {
        publisherSubscriberService.publishToSite(nodeFact.getNode(), message);
    }

    public void sendAbsoluteMessage(String absolute, String message, KnowledgeHelper drools) {
        publisherSubscriberService.publishToAbsoluteChannel(absolute, message);
    }

    public void sendNodeMessage(AddedNodeFact nodeFact, String message, KnowledgeHelper drools) {
        JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(nodeFact.getNode(), "jnt:page");
        if (parentOfType != null) {
            publisherSubscriberService.publishToNodeChannel(parentOfType, message);
        }
    }
}
