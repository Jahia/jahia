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

package org.jahia.services.content;

import org.slf4j.Logger;
import org.jahia.services.categories.Category;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.beans.CategoryBean;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Implementation of JCRValueWrapper Interface.
 *
 * @author : $Author$
 * Last Modified : $Date$
 */
public class JCRValueWrapperImpl implements JCRValueWrapper {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRValueWrapperImpl.class);
    private Value value;
    private final ExtendedPropertyDefinition definition;
    private final JCRSessionWrapper session;

    public JCRValueWrapperImpl(Value value, ExtendedPropertyDefinition definition, JCRSessionWrapper session) {
        this.value = value;
        this.definition = definition;
        this.session = session;
    }

    /**
     * 
     * @return a CategoryBean if value match a valid category
     * @throws ValueFormatException if category not found or object not a category
     * @throws RepositoryException for all other errors
     */
    public CategoryBean getCategory() throws ValueFormatException,RepositoryException {
        try {
            // As we are storing path inside the jcr and taht actually we cannot search by path on the
            // category service we need to get the last value to have the "real" key of the category
            Category category = null;
            if (getType() == PropertyType.STRING) {
                category = Category.getCategory(getString().substring(getString().lastIndexOf("/") + 1));
            } else if (getType() == PropertyType.REFERENCE) {
                category = Category.getCategoryByUUID(getString());
            }
            if (category == null) throw new ValueFormatException(getString() + " is not a valid Jahia Category");
            return new CategoryBean(category);
        } catch (JahiaException e) {
            logger.error("Category not found");
        }

    throw new ItemNotFoundException("category " + getString() + " not found");
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return definition;
    }

    public Date getTime() throws ValueFormatException, RepositoryException {
        return getDate().getTime();
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getString();
    }

    public InputStream getStream() throws IllegalStateException, RepositoryException {
        return value.getStream();
    }

    public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getLong();
    }

    public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getDouble();
    }

    public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getDate();
    }

    public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getBoolean();
    }

    public Binary getBinary() throws RepositoryException {
        return value.getBinary();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return value.getDecimal();
    }

    public JCRNodeWrapper getNode() throws ValueFormatException, IllegalStateException, RepositoryException {
        if (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
            try {
                return session.getNodeByUUID(value.getString());
            } catch (ItemNotFoundException e) {
                return null;
            }
        } else if (definition.getRequiredType() == PropertyType.STRING) {
            try {
                return session.getNodeByUUID(value.getString());
            } catch (ItemNotFoundException e) {
                String path = value.getString();
                try {
                    return (session.getNode(path));
                } catch (PathNotFoundException e1) {
                    return null;
                }
            } 
        } else {
            // TODO: The specification suggests using value conversion
            throw new ValueFormatException("property must be of type REFERENCE or STRING");
        }
    }

    public int getType() {
        return value.getType();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JCRValueWrapperImpl) {
            return value.equals(((JCRValueWrapperImpl)obj).value);
        }
        else return obj != null && obj.getClass().equals(value.getClass()) && value.equals(obj);
    }
}
