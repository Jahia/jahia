/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.tags;

import static org.jahia.api.Constants.JAHIANT_TAG;
import static org.jahia.api.Constants.JAHIAMIX_TAGGED;
import static org.jahia.api.Constants.TAGS;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.utils.ArrayUtils;

/**
 * JCR content tagging service.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingService {

    private static Logger logger = LoggerFactory.getLogger(TaggingService.class);

    private static String getTagsPath(String siteKey) {
        if (siteKey == null || siteKey.length() == 0) {
            throw new IllegalArgumentException("The site key cannot be null or empty.");
        }
        return "/sites/" + siteKey + "/tags";
    }

    /**
     * Creates a new tag node using specified name for the current site.
     * 
     * @param tag
     *            the name of the tag to be created
     * @param siteKey
     *            the site key for the current site
     * @return <code>true</code> if a new tag was created; <code>false</code> in
     *         case the specified tag already exists.
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean createTag(final String tag, final String siteKey) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper existingNode = getTag(tag, siteKey, session);
                if (existingNode == null) {
                    createTag(tag, siteKey, session);
                    session.save();
                }
                return existingNode == null;
            }
        });
    }

    private JCRNodeWrapper createTag(String tag, String siteKey, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = null;
        final String tagsPath = getTagsPath(siteKey);
        JCRNodeWrapper tagTreeNode = session.getNode(tagsPath);
        if (tagTreeNode != null) {
            session.checkout(tagTreeNode);
            // TODO escape the tag node name
            node = tagTreeNode.addNode(tag, JAHIANT_TAG);
        } else {
            logger.error("No tags folder found for the path " + tagsPath + ". Skip creating new tag");
        }
        return node;
    }

    /**
     * Deletes the specified tag node using specified name for the current site.
     * 
     * @param tag
     *            the name of the tag to be deleted
     * @param siteKey
     *            the site key for the current site
     * @return <code>true</code> if the tag node was successfully deleted;
     *         <code>false</code> if the specified tag does not exist.
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean deleteTag(final String tag, final String siteKey) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                final String tagsPath = getTagsPath(siteKey);
                JCRNodeWrapper tagNode = null;
                try {
                    tagNode = session.getNode(tagsPath + "/" + tag);
                } catch (PathNotFoundException ex) {
                    // no tag can be found
                }
                if (tagNode != null) {
                    JCRNodeWrapper parent = tagNode.getParent();
                    session.checkout(parent);
                    session.checkout(tagNode);
                    tagNode.remove();
                    session.save();
                }
                return tagNode != null;
            }
        });
    }

    /**
     * Checks existence of the specified tag.
     * 
     * @param tag
     *            the tag to look up for.
     * @param siteKey
     *            the current site key
     * @return <code>true</code> if the specified tag already exists
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean exists(final String tag, final String siteKey) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper tagNode = getTag(tag, siteKey, session);
                return tagNode != null;
            }
        });
    }

    public JCRNodeWrapper getTag(String tag, String siteKey, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = null;
        try {
            node = session.getNode(getTagsPath(siteKey) + "/" + JCRContentUtils.escapeLocalNodeName(tag));
        } catch (PathNotFoundException ex) {
            // no tag can be found
        }

        return node;
    }

    /**
     * Returns the total number of tag references. Returns <code>-1</code> if
     * the corresponding tag does not exist.
     * 
     * @param tag
     *            the name of the tag to check references
     * @param siteKey
     *            the site key for the current site
     * @return the total number of tag references. Returns <code>-1</code> if
     *         the corresponding tag does not exist.
     * @throws RepositoryException
     *             in case of errors
     */
    public long getTagCount(final String tag, final String siteKey) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Long>() {
            public Long doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper tagNode = getTag(tag, siteKey, session);
                return tagNode != null ? tagNode.getWeakReferences().getSize() : -1;
            }
        });
    }

    /**
     * Tag the current node with the specified tag. The tag value is assigned to
     * the node, if it is not tagged already with the same tag.
     * 
     * @param node
     *            the node to be tagged
     * @param tag
     *            the tag to be used
     * @param siteKey
     *            the key of the current site
     * @param createTagIfNotExists
     *            do we need to create a new tag if the specified does not exist
     *            yet?
     * @return <code>true</code> if the tag was applied to the node
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean tag(final JCRNodeWrapper node, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(node.getSession().getUser().getUsername(), node.getSession().getWorkspace().getName(),((JCRSessionWrapper)node.getSession()).getLocale(), new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return tag(node.getPath(), tag, siteKey, createTagIfNotExists, session);
            }
        });
    }

    /**
     * Tag the current node with the specified tag. The tag value is assigned to
     * the node, if it is not tagged already with the same tag.
     * 
     * @param node
     *            the node to be tagged
     * @param tag
     *            the tag to be used
     * @param siteKey
     *            the key of the current site
     * @param createTagIfNotExists
     *            do we need to create a new tag if the specified does not exist
     *            yet?
     * @param session
     *            the current session
     * @return <code>true</code> if the tag was applied to the node
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists,
            JCRSessionWrapper session) throws RepositoryException {

        boolean applied = false;
        boolean doSessionCommit = false;
        String[] tags = tag.split(",");
        JCRNodeWrapper node = session.getNode(nodePath);
        for (String t : tags) {
            t = t.trim();
            if (!"".equals(t)) {
                JCRNodeWrapper tagNode = getTag(t, siteKey, session);
                if (tagNode == null && createTagIfNotExists) {
                    tagNode = createTag(t, siteKey, session);
                    doSessionCommit = true;
                }
                if (tagNode != null) {
                    Value[] newValues = new Value[]{new ValueImpl(tagNode.getIdentifier(), PropertyType.WEAKREFERENCE)};
                    Value[] values = null;
                    boolean exists = false;
                    if (node.hasProperty(TAGS)) {
                        values = node.getProperty(TAGS).getValues();
                        for (Value existingValue : values) {
                            if (tagNode.getIdentifier().equals(existingValue.getString())) {
                                exists = true;
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        newValues = values != null ? ArrayUtils.join(values, newValues) : newValues;
                        session.checkout(node);
                        if (!node.isNodeType(JAHIAMIX_TAGGED)) {
                            node.addMixin(JAHIAMIX_TAGGED);
                        }
                        node.setProperty(TAGS, newValues);
                        applied = true;
                        doSessionCommit = true;
                    }
                    if (doSessionCommit) {
                        session.save();
                    }
                }
            }
        }
        return applied;
    }

    /**
     * Tag the current node with the specified tag. The tag value is assigned to
     * the node, if it is not tagged already with the same tag.
     * 
     * @param nodePath
     *            the path of the node to be tagged
     * @param tag
     *            the tag to be used
     * @param siteKey
     *            the key of the current site
     * @param createTagIfNotExists
     *            do we need to create a new tag if the specified does not exist
     *            yet?
     * @return <code>true</code> if the tag was applied to the node
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {

        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return tag(nodePath, tag, siteKey, createTagIfNotExists, session);
            }
        });
    }
}
