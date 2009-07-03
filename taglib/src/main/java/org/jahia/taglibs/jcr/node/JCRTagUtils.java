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
package org.jahia.taglibs.jcr.node;

import org.jahia.bin.Jahia;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.utils.LanguageCodeConverters;
import org.apache.log4j.Logger;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 28 mai 2009
 * Time: 15:46:07
 * To change this template use File | Settings | File Templates.
 */
public class JCRTagUtils {
    private static final transient Logger logger = Logger.getLogger(JCRTagUtils.class);

    /**
     * Get the label value depending on the current local
     *
     * @param nodeObject
     * @return
     */
    public static String label(Object nodeObject) {
        return label(nodeObject, Jahia.getThreadParamBean().getLocale());
    }

    /**
     * Get the label value dependind on the local
     *
     * @param nodeObject
     * @param local
     * @return
     */
    public static String label(Object nodeObject, Locale local) {
        if (nodeObject != null) {

            try {
                // case of property
                if (nodeObject instanceof Property) {
                    Property property = (Property) nodeObject;
                    PropertyDefinition propertyDefintion = property.getDefinition();
                    if (propertyDefintion != null && propertyDefintion instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) propertyDefintion;
                        return itemDef.getLabel(local);
                    } else {
                        logger.error("PropertyDefinition doesn't implement 'org.jahia.services.content.nodetypes.ExtendedPropertyDefinition'");
                    }
                }
                // case of PropertyDefinition
                else if (nodeObject instanceof PropertyDefinition) {
                    if (nodeObject instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) nodeObject;
                        return itemDef.getLabel(local);
                    } else {
                        logger.error("PropertyDefinition doesn't implement 'org.jahia.services.content.nodetypes.ExtendedPropertyDefinition'");
                    }
                }
                // case of node type
                else if (nodeObject instanceof NodeType) {
                    NodeType nodeType = (NodeType) nodeObject;
                    if (nodeType instanceof ExtendedNodeType) {
                        ExtendedNodeType extendNodeType = (ExtendedNodeType) nodeType;
                        return extendNodeType.getLabel(local);
                    } else {
                        logger.error("nodeType doesn't implement 'org.jahia.services.content.nodetypes.ExtendedNodeType'");
                    }
                } else {
                    logger.error("Object must be a 'javax.jcr.Property' or 'javax.jcr.nodetype.NodeType'");
                }
            } catch (RepositoryException e) {
                logger.error(e, e);
            }
        }
        return null;
    }

    /**
     * Get the label value dependind on the local
     *
     * @param nodeObject
     * @param locale as a string
     * @return
     */
    public static String label(Object nodeObject, String locale) {
        return label(nodeObject, LanguageCodeConverters.languageCodeToLocale(locale));
    }
    public static boolean isNodeType(JCRNodeWrapper node, String type) {
        try {
            return node.isNodeType(type);
        } catch (RepositoryException e) {
            logger.error(e, e);
            return false;
        }
    }
}
