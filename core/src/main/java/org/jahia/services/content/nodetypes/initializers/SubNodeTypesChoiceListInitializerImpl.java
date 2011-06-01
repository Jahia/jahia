/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;

/**
 * Choice list initializer that looks up direct sub types from the specified type
 * If no param is specified, get the list of nodeTypes
 *
 */
public class SubNodeTypesChoiceListInitializerImpl implements ChoiceListInitializer {
    
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(SubNodeTypesChoiceListInitializerImpl.class);

    @SuppressWarnings("unchecked")
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final SortedSet<ChoiceListValue> listValues = new TreeSet<ChoiceListValue>();
        if (StringUtils.isEmpty(param)) {
            param = "jmix:editorialContent";
        }
        try {
            String includedTypes = StringUtils.substringBefore(param, ";");

            Set<String> excludedTypes = new HashSet<String>();
            String exclusion = StringUtils.substringAfter(param, ";");
            if (StringUtils.isNotBlank(exclusion)) {
                excludedTypes.addAll(CollectionUtils.collect(Arrays.asList(StringUtils.substringAfter(param, ";").split(",")), new Transformer() {
                    public Object transform(Object input) {
                        return ((String) input).trim();
                    }
                }));
            }
            
            for (String nodeTypeName : includedTypes.split(",")) {
                nodeTypeName = nodeTypeName.trim();
                ExtendedNodeType nodeType = NodeTypeRegistry.getInstance()
                        .getNodeType(nodeTypeName);
                if (!isExcludedType(nodeType, excludedTypes)) {
                    listValues.add(new ChoiceListValue(nodeType
                            .getLabel(locale), new HashMap<String, Object>(),
                            new ValueImpl(nodeType.getName(),
                                    PropertyType.STRING, false)));
                }
                NodeTypeIterator nti = nodeType.getSubtypes();
                while (nti.hasNext()) {
                    ExtendedNodeType type = (ExtendedNodeType) nti.next();
                    if (!isExcludedType(type, excludedTypes)) {
                        listValues.add(new ChoiceListValue(type
                                .getLabel(locale),
                                new HashMap<String, Object>(), new ValueImpl(
                                        type.getName(), PropertyType.STRING,
                                        false)));
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Cannot get type", e);
        }
       
        return new ArrayList<ChoiceListValue>(listValues);
    }
    private boolean isExcludedType(ExtendedNodeType nodeType, Set<String> excludedTypes) {
        boolean isExcluded = false;
        for (String excludedType : excludedTypes) {
            if (nodeType.isNodeType(excludedType)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }
    
}
