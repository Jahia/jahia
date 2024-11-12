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
package org.jahia.services.content.interceptor;

import static org.jahia.api.Constants.JCR_LASTMODIFIED;
import static org.jahia.api.Constants.MIX_LAST_MODIFIED;

import java.util.Locale;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

/**
 * Handles last modified date property considering translation sub-nodes.
 * @author toto
 * Date: Aug 5, 2010
 * Time: 6:08:37 PM
 */
public class LastModifiedInterceptor extends BaseInterceptor {

    @Override
    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition)
            throws RepositoryException {
        return node.getSession().getLocale() != null && definition.getDeclaringNodeType().getName().equals(MIX_LAST_MODIFIED);
    }

    @Override
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
            throws ValueFormatException, RepositoryException {
        try {
            JCRNodeWrapper parent = property.getParent();
            Locale locale = property.getSession().getLocale();
            if (!parent.hasI18N(locale)) {
                return storedValue;
            }
            Node i18n = parent.getI18N(locale);
            if (i18n.hasProperty(JCR_LASTMODIFIED)) {
                final boolean isLM = property.getName().equals(JCR_LASTMODIFIED);
                Value lastModified = isLM ? storedValue :
                        property.getParent().getRealNode().getProperty(JCR_LASTMODIFIED).getValue();
                Value i18nLastModified = i18n.getProperty(JCR_LASTMODIFIED).getValue();
                if (i18nLastModified.getDate().after(lastModified.getDate())) {
                    return isLM ? i18nLastModified : i18n.getProperty(property.getName()).getValue();
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return storedValue;
    }

}
