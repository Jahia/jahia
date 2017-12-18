/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;

/**
 * Choice list initializer that looks up direct sub types from the specified type
 * If no param is specified, get the list of nodeTypes
 *
 */
public class NodeTypesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        if (StringUtils.isEmpty(param)) {
            param = "jmix:editorialContent";
        }
        boolean useName = StringUtils.contains(param,";useName");
        NodeTypeIterator nti;
        try {
            List<String> systemIds = null;
            if (StringUtils.contains(param,";fromDependencies")) {
                systemIds = new ArrayList<String>();
                JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
                if (node == null) {
                    node = (JCRNodeWrapper) context.get("contextParent");
                }
                if (node != null) {
                    JCRSiteNode site = node.getResolveSite();
                    if (site != null) {
                        JahiaTemplatesPackage templatePackage = site.getTemplatePackage();
                        systemIds.add(templatePackage.getId());
                        for (JahiaTemplatesPackage dependency : templatePackage.getDependencies()) {
                            systemIds.add(dependency.getId());
                        }
                        systemIds.add("system-extension");
                        systemIds.add("system-standard");
                        systemIds.add("system-jahia");
                        systemIds.add("system-system");
                    }
                }
            }
            if (param.startsWith("MIXIN")) {
                nti = NodeTypeRegistry.getInstance().getMixinNodeTypes(systemIds);
            } else if (param.startsWith("PRIMARY")) {
                nti = NodeTypeRegistry.getInstance().getPrimaryNodeTypes(systemIds);
            } else if (param.startsWith("ALL")) {
                nti = NodeTypeRegistry.getInstance().getAllNodeTypes(systemIds);
            } else {
                ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(param);
                nti = nodeType.getSubtypes();
            }
            while (nti.hasNext()) {
                ExtendedNodeType type = (ExtendedNodeType) nti.next();
                listValues.add(new ChoiceListValue(useName?type.getName():type.getLabel(locale), type.getName()));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get type",e);
        }

        Collections.sort(listValues);
        
        return listValues;
    }
}
