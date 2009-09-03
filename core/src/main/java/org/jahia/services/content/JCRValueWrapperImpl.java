/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.services.categories.Category;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.beans.CategoryBean;
import org.jahia.bin.Jahia;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;

/**
 * Implementation of JCRValueWrapper Interface.
 *
 * @author : $Author$
 * Last Modified : $Date$
 */
public class JCRValueWrapperImpl implements JCRValueWrapper {
    private transient static Logger logger = Logger.getLogger(JCRValueWrapperImpl.class);
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
        if(getType() == PropertyType.STRING) {
            try {
                // As we are storing path inside the jcr and taht actually we cannot search by path on the
                // category service we need to get the last value to have the "real" key of the category
                Category category = Category.getCategory(getString().substring(getString().lastIndexOf("/")+1));
                if(category==null) throw new ValueFormatException(getString()+" is not a valid Jahia Category");
                return new CategoryBean(category, Jahia.getThreadParamBean());
            } catch (JahiaException e) {
                logger.error("Category not found");
            }
        }
        return null;
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

    public Node getNode() throws ValueFormatException, IllegalStateException, RepositoryException {
        if (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
            try {
                return session.getNodeByUUID(value.getString());
            } catch (ItemNotFoundException e) {
                return null;
            }
        } else {
            // TODO: The specification suggests using value conversion
            throw new ValueFormatException("property must be of type REFERENCE");
        }
    }

    public int getType() {
        return value.getType();
    }
}
