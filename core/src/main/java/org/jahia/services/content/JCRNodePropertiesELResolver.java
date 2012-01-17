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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.jcr.RepositoryException;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 13 nov. 2009
 */
public class JCRNodePropertiesELResolver extends ELResolver {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodePropertiesELResolver.class);

    public Object getValue(ELContext elContext, Object base, Object property) {
        if (elContext == null) {
            throw new NullPointerException();
        }
        if (base != null && base instanceof JCRNodeWrapper) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) base;
            try {
                try {
                    nodeWrapper.getClass().getMethod("get" + StringUtils.capitalize(property.toString()));
                } catch (NoSuchMethodException e) {
                    final JCRPropertyWrapper jcrPropertyWrapper = nodeWrapper.getProperty(property.toString().replace(
                            "_", ":"));
                    if (jcrPropertyWrapper != null) {
                        elContext.setPropertyResolved(true);
                        return jcrPropertyWrapper;
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public Class<?> getType(ELContext elContext, Object base, Object property) {
        if (elContext == null) {
            throw new NullPointerException();
        }
        if (base != null && base instanceof JCRNodeWrapper) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) base;
            try {
                try {
                    nodeWrapper.getClass().getMethod("get" + StringUtils.capitalize(property.toString()));
                } catch (NoSuchMethodException e) {
                    if (nodeWrapper.getProperty(property.toString().replace("_", ":")) != null) {
                        elContext.setPropertyResolved(true);
                        return JCRPropertyWrapper.class;
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public void setValue(ELContext elContext, Object base, Object property, Object value) {
    }

    public boolean isReadOnly(ELContext elContext, Object base, Object property) {
        return true;
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elContext, Object base) {
        if (elContext == null) {
            throw new NullPointerException();
        }
        if (base != null && base instanceof JCRNodeWrapper) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) base;
            List<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>();
            try {
                final Set<String> propertyNames = nodeWrapper.getPropertiesAsString().keySet();
                FeatureDescriptor descriptor;
                for (String propertyName : propertyNames) {
                    descriptor = new FeatureDescriptor();
                    descriptor.setDisplayName(propertyName);
                    descriptor.setName(propertyName);
                    descriptor.setShortDescription("");
                    descriptor.setExpert(false);
                    descriptor.setHidden(false);
                    descriptor.setPreferred(true);
                    descriptor.setValue("type", String.class);
                    descriptors.add(descriptor);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            return descriptors.iterator();
        } else {
            return null;
        }
    }

    public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
        return String.class;
    }
}
