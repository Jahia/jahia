/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.interceptor;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;

/**
 * Abstract property interceptor that does not do any value modifications. To be subclassed for particular usage.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseInterceptor implements PropertyInterceptor {

    private Set<Integer> requiredTypes;

    private Set<Integer> selectors;

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
            throws ValueFormatException, RepositoryException {
        return storedValue;
    }

    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues)
            throws ValueFormatException, RepositoryException {
        return storedValues;
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition)
            throws VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        // do nothing
    }

    public Value beforeSetValue(JCRNodeWrapper node, String name,
            ExtendedPropertyDefinition definition, Value originalValue)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return originalValue;
    }

    public Value[] beforeSetValues(JCRNodeWrapper node, String name,
            ExtendedPropertyDefinition definition, Value[] originalValues)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return originalValues;
    }

    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition)
            throws RepositoryException {

        // enforce constraints on the required type and selector type if they were specified
        return (getRequiredTypes() == null || getRequiredTypes().size() == 0 || getRequiredTypes()
                .contains(definition.getRequiredType()))
                && (getSelectors() == null || getSelectors().size() == 0 || getSelectors()
                        .contains(definition.getSelector()));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        BaseInterceptor rhs = (BaseInterceptor) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(getRequiredTypes(), rhs.getRequiredTypes())
                .append(getSelectors(), rhs.getSelectors()).isEquals();
    }

    protected Set<Integer> getRequiredTypes() {
        return requiredTypes;
    }

    protected Set<Integer> getSelectors() {
        return selectors;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 37).append(getRequiredTypes()).append(getSelectors())
                .toHashCode();
    }

    public void setRequiredTypes(Set<String> requiredTypes) {
        if (requiredTypes != null && requiredTypes.size() > 0) {
            this.requiredTypes = new HashSet<Integer>(requiredTypes.size());
            for (String type : requiredTypes) {
                this.requiredTypes.add(PropertyType.valueFromName(type));
            }
        }
    }

    public void setSelectors(Set<String> selectors) {
        if (selectors != null && selectors.size() > 0) {
            this.selectors = new HashSet<Integer>(selectors.size());
            for (String selector : selectors) {
                this.selectors.add(SelectorType.valueFromName(selector));
            }
        }
    }
}
