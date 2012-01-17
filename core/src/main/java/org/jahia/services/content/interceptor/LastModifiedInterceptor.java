/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
