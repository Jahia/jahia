/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.jahia.data.beans.CategoryBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of JCRValueWrapper Interface.
 *
 * @author : $Author$ Last Modified : $Date$
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
     * @return a CategoryBean if value match a valid category
     * @throws ValueFormatException if category not found or object not a category
     * @throws RepositoryException  for all other errors
     */
    public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
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
        if (obj instanceof JCRValueWrapperImpl) {
            return value.equals(((JCRValueWrapperImpl) obj).value);
        }
        // allow equality test on subclasses of Value and invert equality test since value is usually a JCR-proper class which doesn't allow equality checks on subclasses
        else return obj != null && obj instanceof Value && obj.equals(value);
    }
}
