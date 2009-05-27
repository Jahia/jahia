/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.log4j.Logger;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Simple decorator to allow read methods on a JCR node.
 * <p/>
 * User: romain
 * Date: 27 mai 2009
 * Time: 13:56:12
 */
public class JCRNodeReadOnlyDecorator {

    private static final Logger logger = Logger.getLogger(JCRNodeReadOnlyDecorator.class);

    private JCRNodeWrapper node;

    public JCRNodeReadOnlyDecorator(JCRNodeWrapper node) {
        this.node = node;
    }

    public String getName() {
        return node.getName();
    }

    public String getPath() {
        return node.getPath();
    }

    public String getUrl() {
        return node.getUrl();
    }

    public List<JCRNodeWrapper> getChildren() {
        return node.getChildren();
    }

    public boolean isVisible() {
        return node.isVisible();
    }

    public Map<String, String> getPropertiesAsString() {
        return node.getPropertiesAsString();
    }

    public String getPrimaryNodeTypeName() {
        return node.getPrimaryNodeTypeName();
    }

    public List<String> getNodeTypes() {
        return node.getNodeTypes();
    }

    public boolean isCollection() {
        return node.isCollection();
    }

    public boolean isFile() {
        return node.isFile();
    }

    public boolean isPortlet() {
        return node.isPortlet();
    }

    public Date getLastModifiedAsDate() {
        return node.getLastModifiedAsDate();
    }

    public Date getContentLastModifiedAsDate() {
        return node.getContentLastModifiedAsDate();
    }

    public Date getCreationDateAsDate() {
        return node.getCreationDateAsDate();
    }

    public String getCreationUser() {
        return node.getCreationUser();
    }

    public String getModificationUser() {
        return node.getModificationUser();
    }

    public String getPropertyAsString(String name) {
        return node.getPropertyAsString(name);
    }

    public String getPropertyAsString(String namespace, String name) {
        return node.getPropertyAsString(namespace, name);
    }

    public JCRPropertyReadOnlyDecorator getProperty(String property) {
        try {
            Property p = node.getProperty(property);
            return new JCRPropertyReadOnlyDecorator(p);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            return null;
        }
    }

}
