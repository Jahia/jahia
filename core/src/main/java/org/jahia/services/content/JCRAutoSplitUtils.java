/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import static org.jahia.services.content.JCRContentUtils.findAvailableNodeName;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;

/**
 * Utility class for auto-splitting child nodes of a node, based on the provided
 * settings.
 * 
 * @author Thomas Draier 
 * @author Sergiy Shyrkov
 */
public final class JCRAutoSplitUtils {

    private static final Logger logger = Logger.getLogger(JCRAutoSplitUtils.class);

    /**
     * Moves the specified node according to the auto-split policy set on the
     * parent node.<br>
     * Splitting is supported by a particular string property value (e.g.
     * creator, node name etc.), first characters or a part (substring) of the
     * that value. Additionally the date fields are supported.<br>
     * For example the following settings
     * <code>property,jcr:creator;date,jcr:created,yyyy;date,jcr:created,MM</code>
     * will split the child nodes first into folders, based on the creator name
     * (jcr:createdBy), than by creation year (jcr:created) and than by creation
     * month (jcr:created).<br>
     * I.e. the node report.pdf, created by user 'sergiy' on 1st or July 2010,
     * will land under:
     * 
     * <pre>
     *    <parent-node>
     *       |_sergiy
     *               |_2010
     *                     |_07
     *                         |_report.pdf
     * </pre>
     * 
     * The intermediate folders will be created.
     * 
     * @param node the node to be moved
     * @return the moved node
     * @throws RepositoryException in case of an error when relocating node
     */
    public static JCRNodeWrapper applyAutoSplitRules(JCRNodeWrapper node) throws RepositoryException {
        try {
            Node parent = node.getParent();
            String splitConfig = parent.getProperty(Constants.SPLIT_CONFIG).getString();
            String splitType = parent.getProperty(Constants.SPLIT_NODETYPE).getString();
            if (node.isNodeType(splitType)) {
                logger.debug("Aborting auto-splitting since it is applied on the split type.");
                return node;
            }
            String[] config = splitConfig.split(";");
            for (String s : config) {
                String[] folderConfig = s.split(",");

                String type = folderConfig[0];
                String propertyName = folderConfig[1];

                String key = null;

                try {
                    if (type.equals("constant")) {
                        key = propertyName;
                    } else if (type.equals("property")) {
                        if (propertyName.equals("j:nodename")) {
                            key = node.getName();
                            key = StringUtils.substringAfter(key, ":");
                        } else if (node.hasProperty(propertyName)) {
                            key = node.getProperty(propertyName).getString();
                        }
                    } else if (type.equals("firstChars")) {
                        if (propertyName.equals("j:nodename")) {
                            key = node.getName();
                            key = StringUtils.substringAfter(key, ":");
                        } else if (node.hasProperty(propertyName)) {
                            key = node.getProperty(propertyName).getString();
                        }
                        final int index = Integer.parseInt(folderConfig[2]);
                        if (key != null && key.length() > index) {
                            key = key.substring(0, index);
                        }
                    } else if (type.equals("substring")) {
                        if (propertyName.equals("j:nodename")) {
                            key = node.getName();
                            key = StringUtils.substringAfter(key, ":");
                        } else if (node.hasProperty(propertyName)) {
                            key = node.getProperty(propertyName).getString();
                        }
                        String[] indexes = folderConfig[2].split("-");
                        final int startIndex = Integer.parseInt(indexes[0]);
                        final int endIndex = Integer.parseInt(indexes[1]);
                        if (key != null && key.length() > endIndex) {
                            key = key.substring(startIndex, endIndex);
                        }
                    } else if (type.equals("date")) {
                        if (node.hasProperty(propertyName)) {
                            Calendar calendar = node.getProperty(propertyName).getDate();
                            SimpleDateFormat sdf = new SimpleDateFormat(folderConfig[2]);
                            key = sdf.format(calendar.getTime());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Cannot split folder", e);
                    key = null;
                }

                if (key != null) {
                    if (!parent.hasNode(key)) {
                        node.getSession().checkout(parent);
                        parent = parent.addNode(key, splitType);
                    } else {
                        parent = parent.getNode(key);
                    }
                }
            }
            if (!parent.getPath().equals(node.getParent().getPath())) {
                String newPath = parent.getPath() + "/" + findAvailableNodeName(parent, node.getName());
                node.getSession().move(node.getPath(), newPath);
                return node.getSession().getNode(newPath);
            }
            return null;
        } catch (RepositoryException e) {
            logger.error("Error while trying to move node " + node.getPath() , e);
            return null;
        }
    }

    /**
     * Iterates over all sub nodes of the specified node and applies auto-split
     * rules on them.
     * 
     * @param node the node, which children should be moved to split folders
     * @throws RepositoryException in case of an error
     */
    public static void applyAutoSplitRulesOnSubnodes(JCRNodeWrapper node) throws RepositoryException {
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            applyAutoSplitRules((JCRNodeWrapper) ni.nextNode());
        }

    }

    /**
     * Adds a mixin type to the specified node to enable auto-splitting of the
     * child nodes, based on the provided configuration.
     * 
     * @param node the node to enable splitting of children on
     * @param splitConfig auto-splitting settings;
     * @param splitFolderNodeType
     * @throws RepositoryException
     */
    public static void enableAutoSplitting(JCRNodeWrapper node, String splitConfig, String splitFolderNodeType)
            throws RepositoryException {
        node.getSession().checkout(node);
        node.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        node.setProperty(Constants.SPLIT_CONFIG, splitConfig);
        node.setProperty(Constants.SPLIT_NODETYPE, splitFolderNodeType);
        node.getSession().save();
    }
}
