/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.Patterns;

import javax.jcr.nodetype.NoSuchNodeTypeException;
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
                excludedTypes.addAll(CollectionUtils.collect(Arrays.asList(Patterns.COMMA.split(StringUtils.substringAfter(param, ";"))), new Transformer() {
                    public Object transform(Object input) {
                        return ((String) input).trim();
                    }
                }));
            }

            for (String nodeTypeName : Patterns.COMMA.split(includedTypes)) {
                nodeTypeName = nodeTypeName.trim();
                ExtendedNodeType nodeType = NodeTypeRegistry.getInstance()
                        .getNodeType(nodeTypeName);
                if (!isExcludedType(nodeType, excludedTypes)) {
                    listValues.add(new ChoiceListValue(nodeType.getLabel(locale), nodeType
                            .getName()));
                }
                List<ExtendedNodeType> subTypes = nodeType.getSubtypesAsList();
                Set<String> duplicates = findDuplicateLabels(subTypes, locale);
                for (ExtendedNodeType type : subTypes) {
                    if (!isExcludedType(type, excludedTypes)) {
                        String label = type.getLabel(locale);
                        if (duplicates.contains(label)) {
                            label += (" (" + type.getAlias() + ")");
                        }
                        listValues.add(new ChoiceListValue(label, type.getName()));
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Cannot get type", e);
        }

        return new LinkedList<ChoiceListValue>(listValues);
    }
    private boolean isExcludedType(ExtendedNodeType nodeType, Set<String> excludedTypes) {
        if (excludedTypes.contains(nodeType.getName())) {
            return true;
        }
        boolean isExcluded = false;
        for (String excludedType : excludedTypes) {
            if (nodeType.isNodeType(excludedType)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }

    private Set<String> findDuplicateLabels(List<ExtendedNodeType> subTypes, Locale locale) {
        Set<String> duplicates = new HashSet<>();
        Set<String> uniques = new HashSet<>();

        for(ExtendedNodeType subType : subTypes) {
            String label = subType.getLabel(locale);
            if(!uniques.add(label)) {
                duplicates.add(label);
            }
        }

        return duplicates;
    }
}
