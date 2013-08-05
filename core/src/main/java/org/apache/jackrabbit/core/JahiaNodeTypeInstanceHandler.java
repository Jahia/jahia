/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.apache.jackrabbit.core;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.QPropertyDefinition;
import org.apache.jackrabbit.spi.QValue;
import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * User: toto
 * Date: Dec 31, 2009
 * Time: 11:46:21 AM
 */
public class JahiaNodeTypeInstanceHandler extends NodeTypeInstanceHandler {
    private static final Logger logger = LoggerFactory.getLogger(JahiaNodeTypeInstanceHandler.class);
    
    private Calendar created = null;
    private Calendar lastModified = null;
    private String createdBy = null;
    private String lastModifiedBy = null;

    /**
     * userid to use for the "*By" autocreated properties
     */
    private final String userId;

    /**
     * Creates a new node type instance handler.
     * @param userId the user id. if <code>null</code>, {@value #DEFAULT_USERID} is used.
     */
    public JahiaNodeTypeInstanceHandler(String userId) {
        super(userId);
        if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
            userId = userId.substring(JahiaLoginModule.SYSTEM.length());
        }

        this.userId = StringUtils.isEmpty(userId) ? DEFAULT_USERID : userId;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public InternalValue[] computeSystemGeneratedPropertyValues(NodeState parent, QPropertyDefinition def) {
        // add our own auto generated values here
        InternalValue[] genValues = null;

        Name name = def.getName();
        Name declaringNT = def.getDeclaringNodeType();

        if (NameConstants.JCR_CREATED.equals(name)) {
            // jcr:created property of a version or a mix:created
            if (NameConstants.MIX_CREATED.equals(declaringNT)
                    || NameConstants.NT_VERSION.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(created != null ? created : GregorianCalendar.getInstance())};
            }
        } else if (NameConstants.JCR_CREATEDBY.equals(name)) {
            // jcr:createdBy property of a mix:created
            if (NameConstants.MIX_CREATED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(createdBy != null ? createdBy : userId)};
            }
        } else if (NameConstants.JCR_LASTMODIFIED.equals(name)) {
            // jcr:lastModified property of a mix:lastModified
            if (NameConstants.MIX_LASTMODIFIED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(lastModified != null ? lastModified : GregorianCalendar.getInstance())};
            }
        } else if (NameConstants.JCR_LASTMODIFIEDBY.equals(name)) {
            // jcr:lastModifiedBy property of a mix:lastModified
            if (NameConstants.MIX_LASTMODIFIED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(lastModifiedBy != null ? lastModifiedBy : userId)};
            }
        } else {
            genValues = super.computeSystemGeneratedPropertyValues(parent, def);
        }
        return genValues;
    }

    @Override
    public void setDefaultValues(PropertyState property, NodeState parent, QPropertyDefinition def)
            throws RepositoryException {
        InternalValue[] values = computeSystemGeneratedPropertyValues(parent, def);
        QValue[] defaultValues = null;
        if (values == null && (defaultValues = def.getDefaultValues()) != null && defaultValues.length > 0) {
            // retrieve property definition and check if it has dynamic default values
            Name name = def.getName();
            NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
            String propName = name.getNamespaceURI().length() > 0 ? new org.jahia.services.content.nodetypes.Name(
                    name.toString(), ntRegistry.getNamespaces()).toString() : name.getLocalName();
            Name declaringNT = def.getDeclaringNodeType();
            String nodeTypeName = declaringNT.getNamespaceURI().length() > 0 ? new org.jahia.services.content.nodetypes.Name(
                    declaringNT.toString(), ntRegistry.getNamespaces()).toString() : declaringNT.getLocalName();
            ExtendedNodeType nodeType = ntRegistry.getNodeType(nodeTypeName);
            ExtendedPropertyDefinition propertyDefinition = nodeType.getPropertyDefinition(propName);
            if (propertyDefinition != null && propertyDefinition.hasDynamicDefaultValues()) {
                // expand dynamic values
                Value[] expandedDefValues = propertyDefinition.getDefaultValues();
                values = new InternalValue[expandedDefValues.length];
                int i = 0;
                for (Value v : expandedDefValues) {
                    values[i] = InternalValue.create(v, null);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Dynamic values for property {}: {}", name, values[0].getString());
                }
            } else {
                values = InternalValue.create(def.getDefaultValues());
            }
        }
        if (values != null) {
            property.setValues(values);
        }
    }
}
