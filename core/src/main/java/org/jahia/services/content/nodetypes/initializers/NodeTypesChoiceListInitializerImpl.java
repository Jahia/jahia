/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
