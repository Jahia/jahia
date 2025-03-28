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
package org.jahia.services.content;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.utils.LanguageCodeConverters;

import java.util.Set;

/**
 *
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 5/4/11
 */
public class DefaultNameGenerationHelperImpl implements NameGenerationHelper {
    private Set<String> randomizedNames;

    public String generatNodeName(JCRNodeWrapper parent, String nodeType) {
        String defaultName = nodeType.substring(nodeType.lastIndexOf(":") + 1);
        if (getRandomizedNames() != null && getRandomizedNames().contains(nodeType)) {
            defaultName += Math.round(Math.random() * 1000000);
        }
        return JCRContentUtils.findAvailableNodeName(parent, defaultName);
    }

    public String generatNodeName(JCRNodeWrapper parent, String defaultLanguage, ExtendedNodeType nodeType, String targetName) {
        String defaultName = JCRContentUtils.generateNodeName(nodeType.getLabel(
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage)));
        if ((targetName != null) && (!"*".equals(targetName))) {
            // we are in the case of a strongly typed child node, so we use the specified child name instead of the
            // node type.
            defaultName = targetName;
        }
        if (getRandomizedNames()!=null && getRandomizedNames().contains(nodeType.getName())) {
            defaultName += Math.round(Math.random() * 1000000);
        }
        return JCRContentUtils.findAvailableNodeName(parent, defaultName);
    }

    public void setRandomizedNames(Set<String> randomizedNames) {
        this.randomizedNames = randomizedNames;
    }

    public Set<String> getRandomizedNames() {
        return randomizedNames;
    }

}
