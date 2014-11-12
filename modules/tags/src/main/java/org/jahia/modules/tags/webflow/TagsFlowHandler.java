package org.jahia.modules.tags.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
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

    public Map<String,Integer> getTagsList(RenderContext renderContext) {
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
            String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] IS NOT NULL)";
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);
            ScrollableQuery scrollableQuery = new ScrollableQuery(100, q);

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
                            }else {
                                // limit reached
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

                executeActionWithLiveSession(renderContext, messageContext, selectedTag, tagNewName);
                executeActionWithDefaultSession(renderContext, messageContext, selectedTag, tagNewName);
            } catch (RepositoryException e) {
                logger.error("renameAllTags() cannot rename all tags '" + selectedTag + "' by '" + tagNewName + "'");
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
            executeActionWithLiveSession(renderContext, messageContext, selectedTag, null);
            executeActionWithDefaultSession(renderContext, messageContext, selectedTag, null);
        } catch (RepositoryException e) {
            logger.error("deleteAllTags() cannot delete all tags '" + selectedTag + "'");
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
                JCRNodeWrapper node = (JCRNodeWrapper)ni.nextNode();
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
            try {
                JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();

                JCRNodeWrapper node = session.getNodeByIdentifier(nodeID);

                // remove Capital and special character from tag
                tagNewName = taggingService.getTagHandler().execute(tagNewName);
                updateTagsList(renderContext, messageContext, node, selectedTag, tagNewName);
            } catch (RepositoryException e) {
                logger.error("renameTagOnNode() cannot rename tag '" + selectedTag + "'");
            } finally {
                JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
            }
        } else {
            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaTags", "jnt_tagsManager.error.newNameEmpty", renderContext.getUILocale())).build());
        }
    }

    public void deleteTagOnNode(RenderContext renderContext, MessageContext messageContext, String selectedTag, String nodeID) {
        JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();

            JCRNodeWrapper node = session.getNodeByIdentifier(nodeID);

            updateTagsList(renderContext, messageContext, node, selectedTag, null);
        } catch (RepositoryException e) {
            logger.error("deleteTagOnNode() cannot delete tag '" + selectedTag + "'");
        } finally {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
        }
    }

    private void executeAction(RenderContext renderContext, MessageContext messageContext, String selectedTag, String tagNewName, JCRSessionWrapper session) throws RepositoryException {
        String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] = '" + selectedTag + "')";
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper node = (JCRNodeWrapper)ni.nextNode();
            updateTagsList(renderContext, messageContext, node, selectedTag, tagNewName);
        }
    }

    private void executeActionWithDefaultSession(RenderContext renderContext, MessageContext messageContext, String selectedTag, String tagNewName) throws RepositoryException {
        JCRSessionWrapper session = getSystemSessionWorkspace(renderContext, Constants.EDIT_WORKSPACE);
        executeAction(renderContext, messageContext, selectedTag, tagNewName, session);
    }

    private void executeActionWithLiveSession(RenderContext renderContext, MessageContext messageContext, String selectedTag, String tagNewName) throws RepositoryException {
        JCRSessionWrapper session = getSystemSessionWorkspace(renderContext, Constants.LIVE_WORKSPACE);
        executeAction(renderContext, messageContext, selectedTag, tagNewName, session);
    }

    private JCRSessionWrapper getSystemSessionWorkspace(RenderContext renderContext, String selectedWorkspace) throws RepositoryException {
        return sessionFactory.getCurrentSystemSession(selectedWorkspace, renderContext.getMainResourceLocale(), renderContext.getFallbackLocale());
    }

    private static void updateTagsList(RenderContext renderContext, MessageContext messageContext, JCRNodeWrapper node, String selectedTag, String tagNewName) {
        try {
            Set<String> newValues = new TreeSet<String>();
            JCRValueWrapper[] tags = node.getProperty("j:tagList").getValues();
            for (JCRValueWrapper tag : tags) {
                String tagValue = tag.getString();
                if (!tagValue.equals(selectedTag)) {
                    newValues.add(tagValue);
                }
            }
            if (StringUtils.isNotEmpty(tagNewName) && !newValues.contains(tagNewName)) {
                newValues.add(tagNewName);
            }
            node.setProperty("j:tagList", newValues.toArray(new String[newValues.size()]));
            node.getSession().save();
        } catch (RepositoryException e) {
            if (StringUtils.isNotEmpty(tagNewName)) {
                messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.rename", renderContext.getUILocale(), selectedTag, JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName(), node.getPath())).build());
            } else {
                messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.delete", renderContext.getUILocale(), selectedTag, JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName(), node.getPath())).build());
            }
        }
    }
}
