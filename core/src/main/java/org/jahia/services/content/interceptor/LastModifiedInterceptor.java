/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.interceptor;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 5, 2010
 * Time: 6:08:37 PM
 */
public class LastModifiedInterceptor extends BaseInterceptor {
    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition)
            throws RepositoryException {
        return definition.getDeclaringNodeType().getName().equals(Constants.MIX_LAST_MODIFIED) && node.getSession().getLocale() != null;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
            throws ValueFormatException, RepositoryException {
        try {
            Node i18n = property.getParent().getI18N(property.getSession().getLocale());
            if (i18n.hasProperty("jcr:lastModified")) {
                final boolean isLM = property.getName().equals("jcr:lastModified");
                Value lastModified = isLM ? storedValue :
                        property.getParent().getRealNode().getProperty(Constants.JCR_LASTMODIFIED).getValue();
                Value i18nLastModified = i18n.getProperty(Constants.JCR_LASTMODIFIED).getValue();
                if (i18nLastModified.getDate().after(lastModified.getDate())) {
                    return isLM ? i18nLastModified : i18n.getProperty(property.getName()).getValue();
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return storedValue;
    }

}
