/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
