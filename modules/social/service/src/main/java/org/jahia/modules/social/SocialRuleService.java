package org.jahia.modules.social;

import java.util.List;

import javax.jcr.RepositoryException;

import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.JCRSessionWrapper;
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
        JCRSessionWrapper session = nodeFact.getNode().getSession();
        socialService.addActivity(activityType, user, messageKey, nodeFact, nodeTypeList, session);
    }

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, AddedNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        JCRSessionWrapper session = nodeFact.getNode().getSession();
        socialService.sendMessage(fromUser, toUser, subject, message, session);
    }

    /**
     * @param socialService the socialService to set
     */
    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

}
