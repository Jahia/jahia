package org.jahia.modules.tags.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.query.ScrollableQuery;
import org.jahia.services.query.ScrollableQueryCallback;
import org.jahia.services.render.RenderContext;
import org.jahia.services.tags.TaggingService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by dgaillard on 05/11/14.
 */
public class TagsFlowHandler implements Serializable {

    private static final long serialVersionUID = -3325519642397714386L;

    private static final Logger logger = getLogger(TagsFlowHandler.class);

    @Autowired
    private transient TaggingService taggingService;

    @Autowired
    private transient JCRSessionFactory sessionFactory;

    public Map<String, Integer> getTagsList(RenderContext renderContext) {
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
            String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] IS NOT NULL)";
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);
            ScrollableQuery scrollableQuery = new ScrollableQuery(500, q);

            return scrollableQuery.execute(new ScrollableQueryCallback<Map<String, Integer>>() {
                Map<String, Integer> result = new HashMap<String, Integer>();

                @Override
                public boolean scroll() throws RepositoryException {
                    NodeIterator nodeIterator = stepResult.getNodes();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
                        JCRValueWrapper[] tags = nodeWrapper.getProperty("j:tagList").getValues();
                        for (JCRValueWrapper tag : tags) {
                            String tagValue = tag.getString();

                            if (result.containsKey(tagValue)) {
                                result.put(tagValue, result.get(tagValue) + 1);
                            } else {
                                result.put(tagValue, 1);
                            }
                        }
                    }
                    return true;
                }

                @Override
                protected Map<String, Integer> getResult() {
                    return result;
                }
            });
        } catch (RepositoryException e) {
            logger.error("getTags() cannot get Tags List");
            return new HashMap<String, Integer>();
        }
    }

    public void renameAllTags(RenderContext renderContext, MessageContext messageContext, String selectedTag, String tagNewName) {
        if (StringUtils.isNotEmpty(tagNewName)) {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
            try {
                // remove Capital and special character from tag
                tagNewName = taggingService.getTagHandler().execute(tagNewName);
                taggingService.updateOrDeleteTagOnSite(renderContext.getSite().getJCRLocalPath(), selectedTag, tagNewName);
            } finally {
                JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
            }
        } else {
            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaTags", "jnt_tagsManager.error.newNameEmpty", renderContext.getUILocale())).build());
        }
    }

    public void deleteAllTags(RenderContext renderContext, MessageContext messageContext, String selectedTag) {
        JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
        try {
            taggingService.updateOrDeleteTagOnSite(renderContext.getSite().getJCRLocalPath(), selectedTag, null);
        } finally {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
        }
    }

    public Map<String, List<String>> getTagDetails(RenderContext renderContext, String selectedTag) {
        Map<String, List<String>> tagDetails = new HashMap<String, List<String>>();
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();

            String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] = '" + selectedTag + "')";
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);

            NodeIterator ni = q.execute().getNodes();
            List<String> nodeList = new ArrayList<String>();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
                nodeList.add(node.getIdentifier());
            }

            tagDetails.put(selectedTag, nodeList);
            return tagDetails;
        } catch (RepositoryException e) {
            logger.error("getTagDetails() cannot get tag '" + selectedTag + "' details");
            return tagDetails;
        }
    }

    public void renameTagOnNode(RenderContext renderContext, MessageContext messageContext, String selectedTag, String tagNewName, String nodeID) {
        if (StringUtils.isNotEmpty(tagNewName)) {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
            JCRNodeWrapper node = null;
            try {
                // remove Capital and special character from tag
                tagNewName = taggingService.getTagHandler().execute(tagNewName);
                for (String workspace : TaggingService.workspaces) {
                    node = getSystemSessionWorkspace(renderContext, workspace).getNodeByIdentifier(nodeID);
                    taggingService.updateOrDeleteTagOnNode(node, selectedTag, tagNewName);
                }
            } catch (RepositoryException e) {
                if (node != null) {
                    messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.rename", renderContext.getUILocale(), selectedTag, JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName(), node.getPath())).build());
                }
            } finally {
                JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
            }
        } else {
            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaTags", "jnt_tagsManager.error.newNameEmpty", renderContext.getUILocale())).build());
        }
    }

    public void deleteTagOnNode(RenderContext renderContext, MessageContext messageContext, String selectedTag, String nodeID) {
        JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
        JCRNodeWrapper node = null;
        try {
            for (String workspace : TaggingService.workspaces) {
                node = getSystemSessionWorkspace(renderContext, workspace).getNodeByIdentifier(nodeID);
                taggingService.updateOrDeleteTagOnNode(node, selectedTag, null);
            }
        } catch (RepositoryException e) {
            if (node != null) {
                messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.delete", renderContext.getUILocale(), selectedTag, JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName(), node.getPath())).build());
            }
        } finally {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
        }
    }
    private JCRSessionWrapper getSystemSessionWorkspace(RenderContext renderContext, String selectedWorkspace) throws RepositoryException {
        return sessionFactory.getCurrentSystemSession(selectedWorkspace, renderContext.getMainResourceLocale(), renderContext.getFallbackLocale());
    }
}
