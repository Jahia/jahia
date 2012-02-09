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

package org.jahia.services.content;

import static org.jahia.services.content.JCRContentUtils.findAvailableNodeName;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.api.Constants;

/**
 * Utility class for auto-splitting child nodes of a node, based on the provided
 * settings.
 * 
 * @author Thomas Draier 
 * @author Sergiy Shyrkov
 */
public final class JCRAutoSplitUtils {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRAutoSplitUtils.class);

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
        return applyAutoSplitRules(node, node.getParent());
    }

    public static JCRNodeWrapper applyAutoSplitRules(JCRNodeWrapper node, JCRNodeWrapper parent) throws RepositoryException {
        try {
            String splitConfig = parent.getProperty(Constants.SPLIT_CONFIG).getString();
            String splitType = parent.getProperty(Constants.SPLIT_NODETYPE).getString();
            if (node.isNodeType(splitType)) {
                logger.debug("Aborting auto-splitting on node (UUID="+node.getIdentifier()+", path=" + node.getPath() + ") since it is applied on the split type.");
                return null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Auto-splitting node (UUID="+node.getIdentifier()+", path=" + node.getPath() + ") with split config " + splitConfig + " and split node type " + splitType);
            }
            // quick sanity check, to make sure the node hasn't been moved already in the same session !
            try {
                node.getSession().getNode(node.getPath());
            } catch (PathNotFoundException pnfe) {
                logger.warn("Node has already been moved in this session, will not move again !");
                return null;
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
     * @return a map of nodes that were moved, useful to update any code that was using the sub nodes. The key is the
     * previous node bean, and the value is the new node bean.
     * @throws RepositoryException in case of an error
     */
    public static Map<JCRNodeWrapper, JCRNodeWrapper> applyAutoSplitRulesOnSubnodes(JCRNodeWrapper node) throws RepositoryException {
        NodeIterator ni = node.getNodes();
        Map<JCRNodeWrapper, JCRNodeWrapper> modifiedNodes = new HashMap<JCRNodeWrapper, JCRNodeWrapper>();
        while (ni.hasNext()) {
            JCRNodeWrapper existingNode = (JCRNodeWrapper) ni.nextNode();
            JCRNodeWrapper newNode = applyAutoSplitRules(existingNode);
            if (newNode != null) {
                modifiedNodes.put(existingNode, newNode);
            }
        }
        return modifiedNodes;
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

    /**
     * This method uses the valueBean to auto-split the node at creation time, instead of moving it like the other
     * auto-splitting methods do it. This way the node is directly created in the proper location.
     * <br>
     * This method will return the created node in the proper location, or null if the node couldn't be created.
     * <br>
     * Splitting is specified using the splitConfig string, which is used to determine how to name the auto-created
     * intermediary nodes, and uses the format :
     * <code>type,propertyName,type_parameters;type,propertyName,type_parameters;...</code>
     *
     * where type is one of :
     * - constant : the property name will be used as a node name
     * - property : the value of the specified property will be used as a node name
     * - firstChars : takes as a type_parameter the number of characters to use from the value of the property name
     * to split
     * - substring : will use a substring of the property's value. The range is specified using "-" as a separator.
     * - date : will use the type_parameter as a configuration for the SimpleDateFormat parser.
     * the propertyName is the name of the property on which this criteria will operate on. There is a reserved
     * "j:nodename" property name that can be used to reference the nodeName passed to the method.
     *
     * For example the following settings
     * <code>property,creator;date,creationDate,yyyy;date,creationDate,MM</code>
     * and the following bean :
     * <pre>
     * public class MyBean {
     *   public String getCreator();
     *   public Date getCreationDate();
     * }
     * </pre>
     * will split the child nodes first into folders, based on the creator property value
     * (creator), than by creation year (creationDate) and than by creation
     * month (creationDate).<br>
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
     * @param parentNode the parent node in which to auto-split the node that will be created.
     * @param nodeName the node name to use to create the new node. Note that if the node name is already present, this
     * method will call the findAvailableNodeName automatically to append a number to the node.
     * @param nodeType the node type to use when creating the node at the auto-split location.
     * @param splitConfig the auto-splitting configuration
     * @param splitNodeType the node type to use to create the intermediary nodes when auto-splitting.
     * @param valueBean an Object on which the auto-split rules will be applied. This object must be a proper JavaBean
     * object, as BeanUtils method will be used to evaluate the properties specified in the auto-split configuration.
     * @return the newly added node, at the proper auto-split location.
     * @throws RepositoryException if there is an internal error
     */
    public static JCRNodeWrapper addNodeWithAutoSplitting(JCRNodeWrapper parentNode,
                                                          String nodeName,
                                                          String nodeType, 
                                                          String splitConfig,
                                                          String splitNodeType,
                                                          Object valueBean) throws RepositoryException {

        if (logger.isDebugEnabled()) {
            logger.debug("Adding node with auto-splitting (name="+nodeName+", parent path=" + parentNode.getPath() + ") with split config " + splitConfig + " and split node type " + splitNodeType);
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
                        key = nodeName;
                        key = StringUtils.substringAfter(key, ":");
                    } else if (PropertyUtils.getSimpleProperty(valueBean, propertyName) != null) {
                        key = PropertyUtils.getSimpleProperty(valueBean, propertyName).toString();
                    }
                } else if (type.equals("firstChars")) {
                    if (propertyName.equals("j:nodename")) {
                        key = nodeName;
                        key = StringUtils.substringAfter(key, ":");
                    } else if (PropertyUtils.getSimpleProperty(valueBean, propertyName) != null) {
                        key = PropertyUtils.getSimpleProperty(valueBean, propertyName).toString();
                    }
                    final int index = Integer.parseInt(folderConfig[2]);
                    if (key != null && key.length() > index) {
                        key = key.substring(0, index);
                    }
                } else if (type.equals("substring")) {
                    if (propertyName.equals("j:nodename")) {
                        key = nodeName;
                        key = StringUtils.substringAfter(key, ":");
                    } else if (PropertyUtils.getSimpleProperty(valueBean, propertyName) != null) {
                        key = PropertyUtils.getSimpleProperty(valueBean, propertyName).toString();
                    }
                    String[] indexes = folderConfig[2].split("-");
                    final int startIndex = Integer.parseInt(indexes[0]);
                    final int endIndex = Integer.parseInt(indexes[1]);
                    if (key != null && key.length() > endIndex) {
                        key = key.substring(startIndex, endIndex);
                    }
                } else if (type.equals("date")) {
                    if (PropertyUtils.getSimpleProperty(valueBean, propertyName) != null) {
                        Date date = (Date) PropertyUtils.getSimpleProperty(valueBean, propertyName);
                        SimpleDateFormat sdf = new SimpleDateFormat(folderConfig[2]);
                        key = sdf.format(date);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot split folder", e);
                key = null;
            }

            if (key != null) {
                if (!parentNode.hasNode(key)) {
                    parentNode.getSession().checkout(parentNode);
                    parentNode = parentNode.addNode(key, splitNodeType);
                } else {
                    parentNode = parentNode.getNode(key);
                }
            }
        }
        JCRNodeWrapper newNode = parentNode.addNode(findAvailableNodeName(parentNode, nodeName), nodeType);
        logger.debug("Node added at " + newNode.getPath());
        return newNode;
    }

}
