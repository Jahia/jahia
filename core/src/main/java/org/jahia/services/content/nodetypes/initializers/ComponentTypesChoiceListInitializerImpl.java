/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

        if (StringUtils.isNotEmpty(param)) {
            includes = getNodeTypes(StringUtils.substringBefore(param, ";"));
            excludes = getNodeTypes(StringUtils.substringAfter(param, ";"));
        }

        List<ChoiceListValue> choiceList = new LinkedList<ChoiceListValue>();

        JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextNode");
        if (contextNode == null) {
             contextNode = (JCRNodeWrapper) context.get("contextParent");
        }

        try {
            for (Map.Entry<String, String> comp : ComponentRegistry.getComponentTypes(contextNode, includes, excludes, locale).entrySet()) {
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
