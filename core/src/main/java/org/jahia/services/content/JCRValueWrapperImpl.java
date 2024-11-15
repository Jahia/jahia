/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
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

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRValueWrapperImpl.class);

    private Value value;
    private final ExtendedPropertyDefinition definition;
    private final JCRSessionWrapper session;

    public JCRValueWrapperImpl(Value value, ExtendedPropertyDefinition definition, JCRSessionWrapper session) {
        this.value = value;
        this.definition = definition;
        this.session = session;
    }

    @Override
    public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
        try {
            // As we are storing path inside the jcr and taht actually we cannot search by path on the
            // category service we need to get the last value to have the "real" key of the category
            Category category = null;
            if (getType() == PropertyType.STRING) {
                category = Category.getCategory(getString().substring(getString().lastIndexOf('/') + 1));
            } else if (getType() == PropertyType.REFERENCE) {
                category = Category.getCategoryByUUID(getString());
            }
            if (category == null) {
                throw new ValueFormatException(getString() + " is not a valid Jahia Category");
            }
            return new CategoryBean(category);
        } catch (JahiaException e) {
            logger.error("Category not found");
        }
        throw new ItemNotFoundException("category " + getString() + " not found");
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        return definition;
    }

    @Override
    public Date getTime() throws ValueFormatException, RepositoryException {
        return getDate().getTime();
    }

    @Override
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getString();
    }

    @Override
    public InputStream getStream() throws IllegalStateException, RepositoryException {
        return value.getStream();
    }

    @Override
    public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getLong();
    }

    @Override
    public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getDouble();
    }

    @Override
    public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getDate();
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.getBoolean();
    }

    @Override
    public Binary getBinary() throws RepositoryException {
        return value.getBinary();
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return value.getDecimal();
    }

    @Override
    public JCRNodeWrapper getNode() throws ValueFormatException, IllegalStateException, RepositoryException {
        String stringValue = value.getString();
        if (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
            try {
                return session.getNodeByUUID(stringValue);
            } catch (ItemNotFoundException e) {
                return null;
            }
        } else if (definition.getRequiredType() == PropertyType.STRING) {
            try {
                if (StringUtils.startsWith(stringValue, "/")) {
                    return  session.getNode(stringValue);
                }
                return session.getNodeByUUID(stringValue);
            } catch (ItemNotFoundException | PathNotFoundException e) {
                return null;
            }
        } else {
            // TODO: The specification suggests using value conversion
            throw new ValueFormatException("property must be of type REFERENCE or STRING");
        }
    }

    @Override
    public int getType() {
        return value.getType();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && this.getClass() == obj.getClass()) {
            return value.equals(((JCRValueWrapperImpl) obj).value);
        } else {
            // allow equality test on subclasses of Value and invert equality test since value is usually a JCR-proper class which doesn't allow equality checks on subclasses
            return (obj != null && obj instanceof Value && obj.equals(value));
        }
    }

    @Override
    public String toString() {
        try {
            return value.getString();
        } catch (RepositoryException e) {
            return super.toString();
        }
    }
}
