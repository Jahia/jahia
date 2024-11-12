/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.interceptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.JahiaCndReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract property interceptor that does not do any value modifications. To be subclassed for particular usage.
 *
 * @author Sergiy Shyrkov
 */
public abstract class BaseInterceptor implements PropertyInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(BaseInterceptor.class);

    private Set<String> nodeTypes = Collections.emptySet();

    private Set<String> propertyNames = Collections.emptySet();

    private Set<Integer> requiredTypes = Collections.emptySet();

    private Set<Integer> selectors = Collections.emptySet();

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

        // enforce constraints on the property name, required property type, selector type and node type if they were specified

        return (getPropertyNames().size() == 0 || getPropertyNames().contains(definition.getName()))
                && (getRequiredTypes().size() == 0 || getRequiredTypes().contains(
                        definition.getRequiredType()))
                && (getSelectors().size() == 0 || getSelectors().contains(definition.getSelector()))
                && (getNodeTypes().size() == 0 || JCRContentUtils.isNodeType(node, getNodeTypes()));
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
                .append(getNodeTypes(), rhs.getNodeTypes())
                .append(getRequiredTypes(), rhs.getRequiredTypes())
                .append(getSelectors(), rhs.getSelectors())
                .append(getPropertyNames(), rhs.getPropertyNames()).isEquals();
    }

    protected Set<String> getNodeTypes() {
        return nodeTypes;
    }

    protected Set<String> getPropertyNames() {
        return propertyNames;
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
                .append(getNodeTypes()).append(getPropertyNames()).toHashCode();
    }

    public void setNodeTypes(Set<String> nodeTypes) {
        if (nodeTypes != null) {
            this.nodeTypes = nodeTypes;
        } else {
            this.nodeTypes = Collections.emptySet();
        }
    }

    public void setPropertyNames(Set<String> propertyNames) {
        if (propertyNames != null) {
            this.propertyNames = propertyNames;
        } else {
            this.propertyNames = Collections.emptySet();
        }
    }

    public void setRequiredTypes(Set<String> requiredTypes) {
        if (requiredTypes != null && requiredTypes.size() > 0) {
            this.requiredTypes = new HashSet<Integer>(requiredTypes.size());
            for (String type : requiredTypes) {
                int parsedType = JahiaCndReader.getPropertyType(type);
                if (parsedType >= 0) {
                    this.requiredTypes.add(parsedType);
                } else {
                    logger.error("Unknown property type {}. Skipping.", type);
                }
            }
        } else {
            this.requiredTypes = Collections.emptySet();
        }
    }

    public void setSelectors(Set<String> selectors) {
        if (selectors != null && selectors.size() > 0) {
            this.selectors = new HashSet<Integer>(selectors.size());
            for (String selector : selectors) {
                int parsedSelector = JahiaCndReader.getSelectorType(selector);
                if (parsedSelector >= 0) {
                    this.selectors.add(parsedSelector);
                } else {
                    logger.error("Unknown property selector {}. Skipping.", selector);
                }
            }
        } else {
            this.selectors = Collections.emptySet();
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
