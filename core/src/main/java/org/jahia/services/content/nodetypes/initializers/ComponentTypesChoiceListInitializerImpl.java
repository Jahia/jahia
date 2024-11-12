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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Choice list initializer that retrieves a list of allowed UI component types.
 *
 * @author Sergiy Shyrkov
 */
public class ComponentTypesChoiceListInitializerImpl implements ChoiceListInitializer {

    private static final LinkedList<String> DEF_INCLUDES = new LinkedList<String>(
            Arrays.asList("jmix:editorialContent"));

    private static final Logger logger = LoggerFactory
            .getLogger(ComponentTypesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
            List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<String> includes = DEF_INCLUDES;
        List<String> excludes = null;
        boolean restrictedToDependencies = true;

        if (StringUtils.isNotEmpty(param)) {

            if (StringUtils.startsWith(param, "notRestrictedToDependencies;")) {
                restrictedToDependencies = false;
                param = StringUtils.substringAfter(param, "notRestrictedToDependencies;");
            }
            includes = getNodeTypes(StringUtils.substringBefore(param, ";"));
            excludes = getNodeTypes(StringUtils.substringAfter(param, ";"));
        }

        List<ChoiceListValue> choiceList = new LinkedList<ChoiceListValue>();

        JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextNode");
        if (contextNode == null) {
             contextNode = (JCRNodeWrapper) context.get("contextParent");
        }

        try {
            for (Map.Entry<String, String> comp : ComponentRegistry.getComponentTypes(contextNode, includes, excludes, locale, restrictedToDependencies).entrySet()) {
                choiceList.add(new ChoiceListValue(comp.getValue(), comp.getKey()));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return choiceList;
    }

    private List<String> getNodeTypes(String typesString) {
        List<String> types = null;
        if (StringUtils.isNotEmpty(typesString)) {
            types = new LinkedList<String>();
            for (String value : StringUtils.split(typesString, ", ")) {
                if (NodeTypeRegistry.getInstance().hasNodeType(value)) {
                    types.add(value);
                }
            }
        }

        return types;
    }

}
