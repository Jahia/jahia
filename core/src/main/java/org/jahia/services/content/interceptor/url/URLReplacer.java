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
package org.jahia.services.content.interceptor.url;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Map;

/**
 * Replacer used by URLInterceptor to replace refs by placeholders before saving a property, and replace placeholders by refs before reading
 */
public interface URLReplacer {
    /**
     * Return true if replacer can handle tag and attr
     * @param tagName tag name
     * @param attrName attribute name
     * @return true if replacer can handle tag and attribute
     */
    boolean canHandle(String tagName, String attrName);

    /**
     * replace refs by placeholders
     * @return transformed value
     * @throws RepositoryException
     */
    String replaceRefsByPlaceholders(final String originalValue, final Map<String, Long> newRefs, final Map<String, Long> oldRefs, String workspace, final Locale locale, final JCRNodeWrapper node, final ExtendedPropertyDefinition definition) throws RepositoryException;

    /**
     * replace placeholders by refs
     * @return transformed value
     * @throws RepositoryException
     */
    String replacePlaceholdersByRefs(final String originalValue, final Map<Long, String> refs, final String workspaceName, final Locale locale, final JCRNodeWrapper parent) throws RepositoryException;
}
