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
