/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.modules.tags.webflow;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.query.ScrollableQuery;
import org.jahia.services.query.ScrollableQueryCallback;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.tags.TagActionCallback;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.uicomponents.bean.contentmanager.Repository;
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

    public static List<String> workspaces = Arrays.asList(Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);

    @Autowired
    private transient TaggingService taggingService;

    @Autowired
    private transient JCRSessionFactory sessionFactory;

    @Autowired
    private transient ModuleCacheProvider moduleCacheProvider;

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
                for (String workspace : workspaces) {
                    JCRSessionWrapper session = getSystemSessionWorkspace(workspace);
                    Map<String, Set<String>> errors = taggingService.renameTagUnderPath(renderContext.getSite().getJCRLocalPath(), session, selectedTag, tagNewName,
                            new TagManagerActionCallback(moduleCacheProvider, session));

                    for (Map.Entry<String, Set<String>> entry : errors.entrySet()) {
                        for (String path : entry.getValue()) {
                            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.rename", renderContext.getUILocale(), selectedTag, entry.getKey(), path)).build());
                        }
                    }
                }
            } catch (RepositoryException e){
                messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.renameAll.serverIssue", renderContext.getUILocale(), selectedTag, e.getMessage())).build());
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
            for (String workspace : workspaces) {
                JCRSessionWrapper session = getSystemSessionWorkspace(workspace);
                Map<String, Set<String>> errors = taggingService.deleteTagUnderPath(renderContext.getSite().getJCRLocalPath(), getSystemSessionWorkspace(workspace), selectedTag,
                        new TagManagerActionCallback(moduleCacheProvider, session));

                for (Map.Entry<String, Set<String>> entry : errors.entrySet()) {
                    for (String path : entry.getValue()) {
                        messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.delete", renderContext.getUILocale(), selectedTag, entry.getKey(), path)).build());
                    }
                }
            }
        } catch (RepositoryException e){
            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.deleteAll.serverIssue", renderContext.getUILocale(), selectedTag, e.getMessage())).build());
        } finally {
            JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
        }
    }

    public Map<String, List<String>> getTagDetails(RenderContext renderContext, String selectedTag) {
        Map<String, List<String>> tagDetails = new HashMap<String, List<String>>();
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();

            String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] = '" + Text.escapeIllegalXpathSearchChars(selectedTag).replaceAll("'", "''") + "')";
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
            // remove Capital and special character from tag
            tagNewName = taggingService.getTagHandler().execute(tagNewName);

            for (String workspace : workspaces) {
                try {
                        node = getSystemSessionWorkspace(renderContext, workspace).getNodeByIdentifier(nodeID);
                        taggingService.renameTag(node, selectedTag, tagNewName);
                        node.getSession().save();
                } catch (RepositoryException e) {
                    if (node != null) {
                        messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaTags", "jnt_tagsManager.error.rename", renderContext.getUILocale(), selectedTag, JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName(), node.getPath())).build());
                    }
                } finally {
                    JCRObservationManager.setAllEventListenersDisabled(Boolean.FALSE);
                }
            }
        } else {
            messageContext.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaTags", "jnt_tagsManager.error.newNameEmpty", renderContext.getUILocale())).build());
        }
    }

    public void deleteTagOnNode(RenderContext renderContext, MessageContext messageContext, String selectedTag, String nodeID) {
        JCRObservationManager.setAllEventListenersDisabled(Boolean.TRUE);
        JCRNodeWrapper node = null;
        try {
            for (String workspace : workspaces) {
                node = getSystemSessionWorkspace(renderContext, workspace).getNodeByIdentifier(nodeID);
                taggingService.untag(node, selectedTag);
                node.getSession().save();
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

    private JCRSessionWrapper getSystemSessionWorkspace(String selectedWorkspace) throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentSystemSession(selectedWorkspace, Locale.ENGLISH, null);
    }
}
