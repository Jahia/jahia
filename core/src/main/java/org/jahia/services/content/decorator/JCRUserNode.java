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

package org.jahia.services.content.decorator;

import org.jahia.services.content.LazyPropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPropertyWrapperImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.usermanager.JahiaExternalUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserPropertyReadOnlyException;
import org.jahia.services.usermanager.jcr.JCRUser;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represent a user JCR node.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 17 juin 2010
 */
public class JCRUserNode extends JCRNodeDecorator {
    private transient static Logger logger = LoggerFactory.getLogger(JCRUserNode.class);
    private JahiaUser user;
    private Map<String, ExtendedPropertyDefinition> propertyDefinitionMap;
    private Map<Integer, ExtendedPropertyDefinition> unstructuredPropertyDefinitions;

    public final List<String> publicProperties = Arrays.asList("j:external", "j:externalSource", "j:publicProperties");

    public JCRUserNode(JCRNodeWrapper node) {
        super(node);
        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType("jnt:user");
            propertyDefinitionMap = type.getPropertyDefinitionsAsMap();
            unstructuredPropertyDefinitions = type.getUnstructuredPropertyDefinitions();
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        final Locale locale = getSession().getLocale();
        return new LazyPropertyIterator(node, locale, null,null) {
            @Override
            protected PropertyIterator getPropertiesIterator() {
                if (propertyIterator == null) {
                    if (user == null) {
                        user = lookupUser();
                    }
                    if (user == null || user instanceof JCRUser) {
                        propertyIterator = new FilteredPropertyIterator(super.getPropertiesIterator());
                    } else {
                        try {
                            propertyIterator = new UserPropertyIterator(node, new FilteredPropertyIterator(super.getPropertiesIterator()));
                        } catch (RepositoryException e) {
                            logger.error("Cannot get user properties");
                            propertyIterator = new FilteredPropertyIterator(super.getPropertiesIterator());
                        }
                    }
                    propertyIterator = new FilteredPropertyIterator(super.getPropertiesIterator());
                }
                return propertyIterator;
            }

            @Override
            protected PropertyIterator getI18NPropertyIterator() {
                if (i18nPropertyIterator == null) {
                return new FilteredPropertyIterator(super.getI18NPropertyIterator());
                }
                return i18nPropertyIterator;
            }
        };
    }

    @Override
    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        if (JCRUser.J_EXTERNAL.equals(s) || Constants.CHECKIN_DATE.equals(s)) {
            return super.getProperty(s);
        }
        if (!canGetProperty(s)) {
            throw new PathNotFoundException(s);
        }
        if (user == null) {
            user = lookupUser();
        }
        if (user == null || user instanceof JCRUser) {
            return super.getProperty(s);
        } else {
            String property = (user instanceof JahiaExternalUser) ? ((JahiaExternalUser) user).getExternalProperties().getProperty(s) : user.getProperty(s);
            if (null == property) {
                return super.getProperty(s);
            }
            return new JCRPropertyWrapperImpl(node, new JCRUserProperty(s, property), node.getSession(), node.getJCRProvider(),
                    propertyDefinitionMap.get(s) != null ? propertyDefinitionMap.get(s) : unstructuredPropertyDefinitions.get(PropertyType.STRING));
        }
    }

    @Override
    public boolean hasProperty(String s) throws RepositoryException {
        boolean b = super.hasProperty(s);
        return b&canGetProperty(s);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        if (user == null) {
            user = lookupUser();
        }
        Set entries ;
        if (user == null || user instanceof JCRUser) {
            entries = super.getPropertiesAsString().entrySet();
        } else {
            entries = user.getProperties().entrySet();
        }
        Map<String, String> map = new HashMap<String, String>();
        for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (canGetProperty(entry.getKey().toString())) {
                map.put(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }
        return map;
    }

    @Override
    public JCRPropertyWrapper setProperty(String s, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (JCRUser.J_EXTERNAL.equals(s) || Constants.CHECKIN_DATE.equals(s)) {
            return super.setProperty(s, value);
        }
        if (user == null) {
            user = lookupUser();
        }
        if (user == null || user instanceof JCRUser) {
            return super.setProperty(s, value);
        } else {
            if (user instanceof JahiaExternalUser
                    && ((JahiaExternalUser) user).getExternalProperties().hasProperty(s)
                    || !(user instanceof JahiaExternalUser) && user.getProperty(s) != null) {
                throw new AccessDeniedException("Cannot update external property");
            }
            JCRPropertyWrapper prop = super.setProperty(s,value);
            try {
                user.getUserProperties().setProperty(s, value);
            } catch (UserPropertyReadOnlyException e) {
                logger.warn("Cannot set read-only property {} for user {}", s, user.getUserKey());

            }
            return prop;
        }
    }

    private boolean canGetProperty(String s) throws RepositoryException {
        if (!hasPermission("jcr:write") && !publicProperties.contains(s)) {
            if (!super.hasProperty("j:publicProperties")) {
                return false;
            }
            Property p = super.getProperty("j:publicProperties");
            Value[] values = p.getValues();
            for (Value value : values) {
                if (s.equals(value.getString())) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    public class FilteredPropertyIterator implements PropertyIterator {
        private PropertyIterator propertyIterator;

        private Property nextProperty;

        public FilteredPropertyIterator(PropertyIterator propertyIterator) {
            this.propertyIterator = propertyIterator;
            prefetch();
        }

        public Property nextProperty() {
            return (Property) next();
        }

        public void skip(long skipNum) {
            for (int i=0; i<skipNum; i++) {
                next();
            }
        }

        public long getSize() {
            return propertyIterator.getSize();
        }

        public long getPosition() {
            return propertyIterator.getPosition();
        }

        public boolean hasNext() {
            return nextProperty != null;
        }

        public Object next() {
            Property next = nextProperty;
            prefetch();
            return next;
        }

        private void prefetch() {
            try {
                do {
                    if (propertyIterator.hasNext()) {
                        nextProperty = propertyIterator.nextProperty();
                    } else {
                        nextProperty = null;
                    }
                } while (nextProperty != null && !canGetProperty(nextProperty.getName()));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        public void remove() {
            propertyIterator.remove();
        }
    }

    public class UserPropertyIterator implements PropertyIterator {
        Map<String, Property> jcrProperties;
        UserProperties externalProperties;
        private Set<String> stringPropertyNames;
        private Iterator<String> iterator;
        private int index = 0;
        private final JCRNodeWrapper node;

        private UserPropertyIterator(JCRNodeWrapper node, PropertyIterator jcrPropertyIterator) throws RepositoryException {
            this.node = node;
            externalProperties = user instanceof JahiaExternalUser ? ((JahiaExternalUser) user).getExternalProperties() : user.getUserProperties();
            jcrProperties = new HashMap<String, Property>();
            while(jcrPropertyIterator.hasNext()) {
                Property prop = jcrPropertyIterator.nextProperty();
                if (!externalProperties.hasProperty(prop.getName())) {
                    jcrProperties.put(prop.getName(), prop);
                }
            }
            stringPropertyNames = new HashSet<String>(jcrProperties.keySet());
            for (Object key : externalProperties.getProperties().keySet()) {
                stringPropertyNames.add(key.toString());
            }
            iterator = stringPropertyNames.iterator();
        }

        /**
         * Returns the next <code>Property</code> in the iteration.
         *
         * @return the next <code>Property</code> in the iteration.
         * @throws java.util.NoSuchElementException
         *          if iteration has no more <code>Property</code>s.
         */
        public Property nextProperty() {
            String key = iterator.next();
            index++;
            if (externalProperties.hasProperty(key)) {
                String value = externalProperties.getProperty(key);
                try {
                    return new JCRPropertyWrapperImpl(node, new JCRUserProperty(key, value), node.getSession(), node.getJCRProvider(),
                            propertyDefinitionMap.get(key) != null ? propertyDefinitionMap.get(key) : unstructuredPropertyDefinitions.get(PropertyType.STRING));
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (jcrProperties.containsKey(key)) {
                return jcrProperties.get(key);
            }
            throw new NoSuchElementException();
        }

        /**
         * Skip a number of elements in the iterator.
         *
         * @param skipNum the non-negative number of elements to skip
         * @throws java.util.NoSuchElementException
         *          if skipped past the last element in the iterator.
         */
        public void skip(long skipNum) {
            if (skipNum < stringPropertyNames.size()) {
                for (long j = 0; j < skipNum; j++) {
                    iterator.next();
                    index++;
                }
            } else
                throw new NoSuchElementException(skipNum + " is out of bounds for this properties size : " + stringPropertyNames.size());
        }

        /**
         * Returns the total number of of items available through this iterator.
         * For example, for some node <code>N</code>, <code>N.getNodes().getSize()</code>
         * returns the number of child nodes of <code>N</code> visible through the
         * current <code>Session</code>. In some implementations precise information
         * about the number of elements may not be available. In such cases this
         * method must return -1. API clients will then be able to use
         * <code>RangeIterator.getNumberRemaining</code> to get an estimate on the
         * number of elements.
         *
         * @return a long
         */
        public long getSize() {
            return stringPropertyNames.size();
        }

        /**
         * Returns the current position within the iterator. The number
         * returned is the 0-based index of the next element in the iterator,
         * i.e. the one that will be returned on the subsequent <code>next</code> call.
         * <p/>
         * Note that this method does not check if there is a next element,
         * i.e. an empty iterator will always return 0.
         *
         * @return a long
         */
        public long getPosition() {
            return index;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
         */
        public Object next() {
            return nextProperty();
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt>
         *                                       operation is not supported by this Iterator.
         * @throws IllegalStateException         if the <tt>next</tt> method has not
         *                                       yet been called, or the <tt>remove</tt> method has already
         *                                       been called after the last call to the <tt>next</tt>
         *                                       method.
         */
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    public class JCRUserProperty implements Property {
        private final String key;
        private final Object value;

        public JCRUserProperty(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Sets the value of this property to <code>value</code>.
         * If this property's property type is not constrained by the node type of
         * its parent node, then the property type is changed to that of the supplied
         * <code>value</code>. If the property type is constrained, then a
         * best-effort conversion is attempted. If conversion fails, a
         * <code>ValueFormatException</code> is thrown immediately (not on <code>save</code>).
         * The change will be persisted (if valid) on <code>save</code>
         * <p/>
         * A <code>ConstraintViolationException</code> will be thrown either immediately
         * or on <code>save</code>, if the change would violate a node type or implementation-specific constraint.
         * Implementations may differ on when this validation is performed.
         * <p/>
         * A <code>VersionException</code> will be thrown either immediately
         * or on <code>save</code>, if this property belongs to a node that is versionable and
         * checked-in or is non-versionable but whose nearest versionable ancestor is checked-in.
         * Implementations may differ on when this validation is performed.
         * <p/>
         * A <code>LockException</code> will be thrown either immediately
         * or on <code>save</code>, if a lock prevents the setting of the value.
         * Implementations may differ on when this validation is performed.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(Value value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to the <code>values</code> array.
         * If this property's property type is not constrained by the node type of
         * its parent node, then the property type may be changed. If the property type is constrained, then a
         * best-effort conversion is attempted, according to an implemention-dependent
         * definition of "best effort". If conversion fails, a
         * <code>ValueFormatException</code> is thrown immediately (not on <code>save</code>).
         * If this property is not a multi-valued then a <code>ValueFormatException</code> is
         * thrown immediately. The change will be persisted (if valid) on <code>save</code>.
         * <p/>
         * A <code>ConstraintViolationException</code> will be thrown either immediately
         * or on <code>save</code>, if the change would violate a node type or implementation-specific constraint.
         * Implementations may differ on when this validation is performed.
         * <p/>
         * A <code>VersionException</code> will be thrown either immediately
         * or on <code>save</code>, if this property belongs to a node that is versionable and
         * checked-in or is non-versionable but whose nearest versionable ancestor is checked-in.
         * Implementations may differ on when this validation is performed.
         * <p/>
         * A <code>LockException</code> will be thrown either immediately
         * or on <code>save</code>, if a lock prevents the setting of the value.
         * Implementations may differ on when this validation is performed.
         *
         * @param values The new values to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified values
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(Value[] values)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>String</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(String value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to the <code>values</code> array.
         * Same as <code>{@link #setValue(javax.jcr.Value[] values)}</code> except that the
         * values are specified as a <code>String[]</code>.
         *
         * @param values The new values to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified values
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(String[] values)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as an <code>InputStream</code>.
         * <p/>
         * The passed stream is closed before this method returns either normally or
         * because of an exception.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         * @deprecated As of JCR 2.0, {@link #setValue(javax.jcr.Binary)} should be used instead.
         */
        public void setValue(InputStream value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>Binary</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         * @since JCR 2.0
         */
        public void setValue(Binary value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>long</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(long value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>double</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(double value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>BigDecimal</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property or is actually a subclass of <code>BigDecimal</code>.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         * @since JCR 2.0
         */
        public void setValue(BigDecimal value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>Calendar</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(Calendar value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets the value of this property to <code>value</code>.
         * Same as <code>{@link #setValue(javax.jcr.Value value)}</code> except that the
         * value is specified as a <code>boolean</code>.
         *
         * @param value The new value to set the property to.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(boolean value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Sets this REFERENCE property to refer to the specified node. If
         * this property is not of type REFERENCE or the specified node is
         * not referenceable (i.e., is not of mixin node type
         * <code>mix:referenceable</code> and therefore does not have a UUID) then a
         * <code>ValueFormatException</code> is thrown.
         *
         * @param value The node to which this REFERENCE property will refer.
         * @throws javax.jcr.ValueFormatException if the type or format of the specified value
         *                                        is incompatible with the type of this property.
         * @throws javax.jcr.version.VersionException
         *                                        if this property belongs to a node that is versionable and checked-in
         *                                        or is non-versionable but whose nearest versionable ancestor is checked-in and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the value and this
         *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                        if the change would violate a node-type or other constraint
         *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public void setValue(Node value)
                throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        }

        /**
         * Returns the value of this  property as a <code>Value</code> object.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * The object returned is a copy of the stored value and is immutable.
         *
         * @return the value
         * @throws javax.jcr.ValueFormatException if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public Value getValue() throws ValueFormatException, RepositoryException {
            return new ValueImpl(value.toString(), PropertyType.STRING);
        }

        /**
         * Returns an array of all the values of this property. Used to access
         * multi-value properties. If the property is single-valued, this method throws a
         * <code>ValueFormatException</code>. The array returned is a copy of the stored
         * values, so changes to it are not reflected in internal storage.
         *
         * @return a <code>Value</code> array
         * @throws javax.jcr.ValueFormatException if the property is single-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public Value[] getValues() throws ValueFormatException, RepositoryException {
            return new Value[0];
        }

        /**
         * Returns a <code>String</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getString()</code>. See {@link javax.jcr.Value}.
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a <code>String</code>,
         * a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A string representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a <code>String</code> is
         *                                        not possible or if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public String getString() throws ValueFormatException, RepositoryException {
            return value.toString();
        }

        /**
         * Returns an <code>InputStream</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getStream()</code>. See {@link javax.jcr.Value}.
         * <p/>
         * It is the responsibility of the caller to close the returned InputStream.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A stream representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         * @deprecated As of JCR 2.0, {@link #getBinary()} should be used instead.
         */
        public InputStream getStream() throws ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * Returns a <code>Binary</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getBinary()</code>. See {@link javax.jcr.Value}.
         * <p/>
         * It is the responsibility of the caller to close the returned InputStream.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A stream representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         * @since JCR 2.0
         */
        public Binary getBinary() throws ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * Returns a <code>long</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getLong()</code>. See {@link javax.jcr.Value}.
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a
         * <code>long</code>, a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A <code>long</code> representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a <code>long</code> is not
         *                                        possible or if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         */
        public long getLong() throws ValueFormatException, RepositoryException {
            return 0;
        }

        /**
         * Returns a <code>double</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getDouble()</code>. See {@link javax.jcr.Value}.
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a <code>double</code>,
         * a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A double representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a <code>double</code> is
         *                                        not possible or if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         */
        public double getDouble() throws ValueFormatException, RepositoryException {
            return 0;
        }

        /**
         * Returns a <code>BigDecimal</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getDecimal()</code>. See {@link javax.jcr.Value}.
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a <code>BigDecimal</code>,
         * a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A <code>BigDecimal</code> representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a <code>BigDecimal</code>
         *                                        is not possible or if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         * @since JCR 2.0
         */
        public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * Returns a <code>Calendar</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getDate()</code>. See {@link javax.jcr.Value}.
         * <p/>
         * The object returned is a copy of the stored value, so changes to it are not reflected in internal storage.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a
         * <code>Calendar</code>, a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A date (<code>Calendar</code> object)  representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a string is not possible or if the
         *                                        property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         */
        public Calendar getDate() throws ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * Returns a <code>boolean</code> representation of the value of this
         * property. A shortcut for
         * <code>Property.getValue().getBoolean()</code>. See {@link javax.jcr.Value}.
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If the value of this property cannot be converted to a <code>boolean</code>,
         * a <code>ValueFormatException</code> is thrown.
         * <p/>
         * A <code>RepositoryException</code> is thrown if another error occurs.
         *
         * @return A <code>boolean</code> representation of the value of this property.
         * @throws javax.jcr.ValueFormatException if conversion to a <code>boolean</code> is
         *                                        not possible or if the property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs
         */
        public boolean getBoolean() throws ValueFormatException, RepositoryException {
            return false;
        }

        /**
         * If this property is of type <code>REFERENCE</code>, <code>WEAKREFERENCE</code> or <code>PATH</code>
         * (or convertible to one of these types) this method returns the <code>Node</code> to which this property refers.
         * <p/>
         * If this property is of type <code>PATH</code> and it contains a relative path, it is interpreted relative to the
         * parent node of this property. For example "<code>.</code>" refers to the parent node itself, "<code>..</code>" to
         * the parent of the parent node and "<code>foo</code>" to a sibling node of this property.
         * <p/>
         * If this property is of type <code>WEAKREFERENCE</code> and no node exists in this workspace with the specified
         * UUID then an <code>ItemNotFoundException</code> is thrown.
         * <p/>
         * If this property is of type <code>PATH</code> and no node accessible by the current <code>Session</code>exists
         * in this workspace at the specified path then an <code>ItemNotFoundException</code> is thrown. Note that this
         * applies even if a <i>property</i> exists at the specified location. To dereference to a target property (as
         * opposed to a target node), the method <code>Property.getProperty</code> is used.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If this property cannot be converted to a <code>REFERENCE</code>, <code>WEAKREFERENCE</code> or <code>PATH</code>
         * then a <code>ValueFormatException</code> is thrown.
         * <p/>
         * If this property is currently part of the frozen state of a version in version storage, this method will throw a
         * <code>ValueFormatException</code>.
         *
         * @return the referenced Node
         * @throws javax.jcr.ValueFormatException if this property cannot be converted to a referring type (<code>REFERENCE</code>,
         *                                        <code>WEAKREFERENCE</code> or PATH), if the property is multi-valued or if this property is a referring type
         *                                        but is currently part of the frozen state of a version in version storage.
         * @throws javax.jcr.ItemNotFoundException
         *                                        If this property is of type <code>PATH</code> and no node accessible by the current <code>Session</code> exists
         *                                        in this workspace at the specified path.
         * @throws javax.jcr.RepositoryException  if another error occurs
         */
        public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * If this property is of type <code>PATH</code> (or convertible to this type) this method returns the
         * <code>Property</code> to which <i>this</i> property refers.
         * <p/>
         * If this property contains a relative path, it is interpreted relative to the parent node of this property.
         * For example "<code>.</code>" refers to the parent node itself, "<code>..</code>" to the parent of the parent node
         * and "<code>foo</code>" to a sibling property of this property or this property itself.
         * <p/>
         * If no property exists in this workspace at the specified path then an <code>ItemNotFoundException</code> is
         * thrown. Note that this applies even if a <i>node</i> exists at the specified location. To dereference to a target
         * node, the method <code>Property.getNode</code> is used.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * If this property cannot be converted to a <code>PATH</code> then a <code>ValueFormatException</code> is thrown.
         * <p/>
         * If this property is currently part of the frozen state of a version in version storage, this method will throw a
         * <code>ValueFormatException</code>.
         *
         * @return the referenced property
         * @throws javax.jcr.ValueFormatException if this property cannot be converted to a <code>PATH</code>, if the property is multi-valued or if this property is a referring type
         *                                        but is currently part of the frozen state of a version in version storage.
         * @throws javax.jcr.ItemNotFoundException
         *                                        If this property is of type <code>PATH</code> and no property accessible by the current <code>Session</code> exists
         *                                        in this workspace at the specified path.
         * @throws javax.jcr.RepositoryException  if another error occurs
         * @since JCR 2.0
         */
        public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
            return null;
        }

        /**
         * Returns the length of the value of this property.
         * <p>
         * For a <code>BINARY</code> property, <code>getLength</code> returns the number of bytes.
         * For other property types, <code>getLength</code> returns the same value that would be
         * returned by calling {@link String#length()} on the value when it has been
         * converted to a <code>STRING</code> according to standard JCR propety type conversion.
         * </p>
         * Returns -1 if the implementation cannot determine the length.
         * <p/>
         * If this property is multi-valued, this method throws a <code>ValueFormatException</code>.
         *
         * @return an <code>long</code>.
         * @throws javax.jcr.ValueFormatException if this property is multi-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public long getLength() throws ValueFormatException, RepositoryException {
            return 0;
        }

        /**
         * Returns an array holding the lengths of the values of this (multi-value) property in bytes
         * where each is individually calculated as described in {@link #getLength()}.
         * <p/>
         * Returns a <code>-1</code> in the appropriate position if the implementation cannot determine
         * the length of a value.
         * <p/>
         * If this property is single-valued, this method throws a <code>ValueFormatException</code>.
         * <p/>
         * A RepositoryException is thrown if another error occurs.
         *
         * @return an array of lengths
         * @throws javax.jcr.ValueFormatException if this property is single-valued.
         * @throws javax.jcr.RepositoryException  if another error occurs.
         */
        public long[] getLengths() throws ValueFormatException, RepositoryException {
            return new long[0];
        }

        /**
         * Returns the property definition that applies to this property. In some cases there may appear to
         * be more than one definition that could apply to this node. However, it is assumed that upon
         * creation or change of this property, a single particular definition is chosen by the implementation.
         * It is <i>that</i> definition that this method returns. How this governing definition is selected upon property
         * creation or change from among others which may have been applicable is an implementation issue and is not
         * covered by this specification.
         *
         * @return a <code>PropertyDefinition</code> object.
         * @throws javax.jcr.RepositoryException if an error occurs.
         * @see javax.jcr.nodetype.NodeType#getPropertyDefinitions
         */
        public PropertyDefinition getDefinition() throws RepositoryException {
            return null;
        }

        /**
         * Returns the type of this <code>Property</code>. One of:
         * <ul>
         * <li><code>PropertyType.STRING</code></li>
         * <li><code>PropertyType.BINARY</code></li>
         * <li><code>PropertyType.DATE</code></li>
         * <li><code>PropertyType.DOUBLE</code></li>
         * <li><code>PropertyType.LONG</code></li>
         * <li><code>PropertyType.BOOLEAN</code></li>
         * <li><code>PropertyType.NAME</code></li>
         * <li><code>PropertyType.PATH</code></li>
         * <li><code>PropertyType.REFERENCE</code></li>
         * <li><code>PropertyType.WEAKREFERENCE</code></li>
         * <li><code>PropertyType.URI</code></li>
         * </ul>
         * The type returned is that which was set at property creation. Note that for some property <code>p</code>,
         * the type returned by <code>p.getType()</code> will differ from the type returned by
         * <code>p.getDefinition.getRequiredType()</code> only in the case where the latter returns <code>UNDEFINED</code>.
         * The type of a property instance is never <code>UNDEFINED</code> (it must always have some actual type).
         *
         * @return an int
         * @throws javax.jcr.RepositoryException if an error occurs
         */
        public int getType() throws RepositoryException {
            return PropertyType.STRING;
        }

        public boolean isMultiple() throws RepositoryException {
            return false;
        }

        /**
         * Returns the absolute path to this item.
         * If the path includes items that are same-name sibling nodes properties
         * then those elements in the path will include the appropriate
         * "square bracket" index notation (for example, <code>/a/b[3]/c</code>).
         *
         * @return the path of this <code>Item</code>.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public String getPath() throws RepositoryException {
            return null;
        }

        /**
         * Returns the name of this <code>Item</code>. The name of an item is the
         * last element in its path, minus any square-bracket index that may exist.
         * If this <code>Item</code> is the root node of the workspace (i.e., if
         * <code>this.getDepth() == 0</code>), an empty string will be returned.
         * <p/>
         *
         * @return the (or a) name of this <code>Item</code> or an empty string
         *         if this <code>Item</code> is the root node.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public String getName() throws RepositoryException {
            return key;
        }

        /**
         * Returns the ancestor of the specified depth.
         * An ancestor of depth <i>x</i> is the <code>Item</code> that is <i>x</i>
         * levels down along the path from the root node to <i>this</i>
         * <code>Item</code>.
         * <ul>
         * <li><i>depth</i> = 0 returns the root node.
         * <li><i>depth</i> = 1 returns the child of the root node along the path
         * to <i>this</i> <code>Item</code>.
         * <li><i>depth</i> = 2 returns the grandchild of the root node along the
         * path to <i>this</i> <code>Item</code>.
         * <li>And so on to <i>depth</i> = <i>n</i>, where <i>n</i> is the depth
         * of <i>this</i> <code>Item</code>, which returns <i>this</i>
         * <code>Item</code> itself.
         * </ul>
         * If <i>depth</i> &gt; <i>n</i> is specified then a
         * <code>ItemNotFoundException</code> is thrown.
         * <p/>
         *
         * @param depth An integer, 0 &lt;= <i>depth</i> &lt;= <i>n</i> where <i>n</i> is the depth
         *              of <i>this</i> <code>Item</code>.
         * @return The ancestor of this
         *         <code>Item</code> at the specified <code>depth</code>.
         * @throws javax.jcr.ItemNotFoundException
         *                                       if <i>depth</i> &lt; 0 or
         *                                       <i>depth</i> &gt; <i>n</i> where <i>n</i> is the is the depth of
         *                                       this item.
         * @throws javax.jcr.AccessDeniedException
         *                                       if the current session does not have
         *                                       sufficient access rights to retrieve the specified node.
         * @throws javax.jcr.RepositoryException if another error occurs.
         */
        public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return null;
        }

        /**
         * Returns the parent of this <code>Item</code>.
         *
         * @return The parent of this <code>Item</code>.
         * @throws javax.jcr.ItemNotFoundException
         *                                       if there is no parent.  This only happens
         *                                       if this item is the root node of a workspace.
         * @throws javax.jcr.AccessDeniedException
         *                                       if the current session does not have
         *                                       sufficient access rights to retrieve the parent of this item.
         * @throws javax.jcr.RepositoryException if another error occurs.
         */
        public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return null;
        }

        /**
         * Returns the depth of this <code>Item</code> in the workspace tree.
         * Returns the depth below the root node of <i>this</i> <code>Item</code>
         * (counting <i>this</i> <code>Item</code> itself).
         * <ul>
         * <li>The root node returns 0.
         * <li>A property or child node of the root node returns 1.
         * <li>A property or child node of a child node of the root returns 2.
         * <li>And so on to <i>this</i> <code>Item</code>.
         * </ul>
         *
         * @return The depth of this <code>Item</code> in the workspace hierarchy.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public int getDepth() throws RepositoryException {
            return 0;
        }

        /**
         * Returns the <code>Session</code> through which this <code>Item</code>
         * was acquired.
         * Every <code>Item</code> can ultimately be traced back through a series
         * of method calls to the call <code>{@link javax.jcr.Session#getRootNode}</code>,
         * <code>{@link javax.jcr.Session#getItem}</code> or
         * <code>{@link javax.jcr.Session#getNodeByUUID}</code>. This method returns that
         * <code>Session</code> object.
         *
         * @return the <code>Session</code> through which this <code>Item</code> was
         *         acquired.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public Session getSession() throws RepositoryException {
            return null;
        }

        /**
         * Indicates whether this <code>Item</code> is a <code>Node</code> or a
         * <code>Property</code>.
         * Returns <code>true</code> if this <code>Item</code> is a <code>Node</code>;
         * Returns <code>false</code> if this <code>Item</code> is a <code>Property</code>.
         *
         * @return <code>true</code> if this <code>Item</code> is a
         *         <code>Node</code>, <code>false</code> if it is a <code>Property</code>.
         */
        public boolean isNode() {
            return false;
        }

        /**
         * Returns <code>true</code> if this is a new item, meaning that it exists only in transient
         * storage on the <code>Session</code> and has not yet been saved. Within a transaction,
         * <code>isNew</code> on an <code>Item</code> may return <code>false</code> (because the item
         * has been saved) even if that <code>Item</code> is not in persistent storage (because the
         * transaction has not yet been committed).
         * <p/>
         * Note that if an item returns <code>true</code> on <code>isNew</code>,
         * then by definition is parent will return <code>true</code> on <code>isModified</code>.
         * <p/>
         * Note that in level 1 (that is, read-only) implementations,
         * this method will always return <code>false</code>.
         *
         * @return <code>true</code> if this item is new; <code>false</code> otherwise.
         */
        public boolean isNew() {
            return false;
        }

        /**
         * Returns <code>true</code> if this <code>Item</code> has been saved but has subsequently
         * been modified through the current session and therefore the state of this item as recorded
         * in the session differs from the state of this item as saved. Within a transaction,
         * <code>isModified</code> on an <code>Item</code> may return <code>false</code> (because the
         * <code>Item</code> has been saved since the modification) even if the modification in question
         * is not in persistent storage (because the transaction has not yet been committed).
         * <p/>
         * Note that in level 1 (that is, read-only) implementations,
         * this method will always return <code>false</code>.
         *
         * @return <code>true</code> if this item is modified; <code>false</code> otherwise.
         */
        public boolean isModified() {
            return false;
        }

        /**
         * Returns <code>true</code> if this <code>Item</code> object
         * (the Java object instance) represents the same actual workspace item as the
         * object <code>otherItem</code>.
         * <p/>
         * Two <code>Item</code> objects represent the same workspace item if all the following
         * are true:
         * <ul>
         * <li>Both objects were acquired through <code>Session</code> objects that were created
         * by the same <code>Repository</code> object.</li>
         * <li>Both objects were acquired through <code>Session</code> objects bound to the same
         * repository workspace.</li>
         * <li>The objects are either both <code>Node</code> objects or both <code>Property</code>
         * objects.</li>
         * <li>If they are <code>Node</code> objects, they have the same correspondence identifier.
         * Note that this is the identifier used to determine whether two nodes in different
         * workspaces correspond but obviously it is also true that any node has the same
         * correspondence identifier as itself. Hence, this identifier is used here to
         * determine whether two different Java <code>Node</code> objects actually represent the same
         * workspace node.</li>
         * <li>If they are <code>Property</code> objects they have identical names and
         * <code>isSame</code> is true of their parent nodes.</li>
         * </ul>
         * This method does not compare the <i>states</i> of the two items. For example, if two
         * <code>Item</code> objects representing the same actual workspace item have been
         * retrieved through two different sessions and one has been modified, then this method
         * will still return <code>true</code> when comparing these two objects. Note that if two
         * <code>Item</code> objects representing the same workspace item
         * are retrieved through the <i>same</i> session they will always reflect the
         * same state (see section 5.1.3 <i>Reflecting Item State</i> in the JSR 283 specification
         * document) so comparing state is not an issue.
         *
         * @param otherItem the <code>Item</code> object to be tested for identity with this <code>Item</code>.
         * @return <code>true</code> if this <code>Item</code> object and <code>otherItem</code> represent the same actual repository
         *         item; <code>false</code> otherwise.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public boolean isSame(Item otherItem) throws RepositoryException {
            return false;
        }

        /**
         * Accepts an <code>ItemVistor</code>.
         * Calls the appropriate <code>ItemVistor</code>
         * <code>visit</code> method of the according to whether <i>this</i>
         * <code>Item</code> is a <code>Node</code> or a <code>Property</code>.
         *
         * @param visitor The ItemVisitor to be accepted.
         * @throws javax.jcr.RepositoryException if an error occurs.
         */
        public void accept(ItemVisitor visitor) throws RepositoryException {

        }

        /**
         * Validates all pending changes currently recorded in this <code>Session</code> that apply to this <code>Item</code>
         * or any of its descendants (that is, the subtree rooted at this Item). If validation of <i>all</i>
         * pending changes succeeds, then this change information is cleared from the <code>Session</code>.
         * If the <code>save</code> occurs outside a transaction, the changes are persisted and thus
         * made visible to other <code>Sessions</code>. If the <code>save</code> occurs within a transaction,
         * the changes are not persisted until the transaction is committed.
         * <p/>
         * If validation fails, then no pending changes are saved and they remain recorded on the <code>Session</code>.
         * There is no best-effort or partial save.
         * <p/>
         * The item in persistent storage to which a transient item is saved is
         * determined by matching identifiers and paths.
         * <p/>
         * An <code>AccessDeniedException</code> will be thrown if any of the changes
         * to be persisted would violate the access privileges of this
         * <code>Session</code>.
         * <p/>
         * If any of the changes to be persisted would cause the removal of a node
         * that is currently the target of a <code>REFERENCE</code> property then a
         * <code>ReferentialIntegrityException</code> is thrown, provided that this <code>Session</code> has
         * read access to that <code>REFERENCE</code> property. If, on the other hand, this
         * <code>Session</code> does not have read access to the <code>REFERENCE</code> property in question,
         * then an <code>AccessDeniedException</code> is thrown instead.
         * <p/>
         * An <code>ItemExistsException</code> will be thrown if any of the changes
         * to be persisted would be prevented by the presence of an already existing
         * item in the workspace.
         * <p/>
         * A <code>ConstraintViolationException</code> will be thrown if any of the
         * changes to be persisted would violate a node type restriction.
         * Additionally, a repository may use this exception to enforce
         * implementation- or configuration-dependant restrictions.
         * <p/>
         * An <code>InvalidItemStateException</code> is thrown if any of the
         * changes to be persisted conflicts with a change already persisted
         * through another session and the implementation is such that this
         * conflict can only be detected at <code>save</code>-time and therefore was not
         * detected earlier, at change-time.
         * <p/>
         * A <code>VersionException</code> is thrown if the <code>save</code> would make a result in
         * a change to persistent storage that would violate the read-only status of a
         * checked-in node.
         * <p/>
         * A <code>LockException</code> is thrown if the <code>save</code> would result
         * in a change to persistent storage that would violate a lock.
         * <p/>
         * A <code>NoSuchNodeTypeException</code> is thrown if the <code>save</code> would result in the
         * addition of a node with an unrecognized node type.
         * <p/>
         * A <code>RepositoryException</code> will be thrown if another error
         * occurs.
         *
         * @throws javax.jcr.AccessDeniedException
         *                                       if any of the changes to be persisted would violate
         *                                       the access privileges of the this <code>Session</code>. Also thrown if  any of the
         *                                       changes to be persisted would cause the removal of a node that is currently
         *                                       referenced by a <code>REFERENCE</code> property that this Session
         *                                       <i>does not</i> have read access to.
         * @throws javax.jcr.ItemExistsException if any of the changes
         *                                       to be persisted would be prevented by the presence of an already existing
         *                                       item in the workspace.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                       if any of the changes to be persisted would
         *                                       violate a node type or restriction. Additionally, a repository may use this
         *                                       exception to enforce implementation- or configuration-dependent restrictions.
         * @throws javax.jcr.InvalidItemStateException
         *                                       if any of the
         *                                       changes to be persisted conflicts with a change already persisted
         *                                       through another session and the implementation is such that this
         *                                       conflict can only be detected at <code>save</code>-time and therefore was not
         *                                       detected earlier, at change-time.
         * @throws javax.jcr.ReferentialIntegrityException
         *                                       if any of the
         *                                       changes to be persisted would cause the removal of a node that is currently
         *                                       referenced by a <code>REFERENCE</code> property that this <code>Session</code>
         *                                       has read access to.
         * @throws javax.jcr.version.VersionException
         *                                       if the <code>save</code> would make a result in
         *                                       a change to persistent storage that would violate the read-only status of a
         *                                       checked-in node.
         * @throws javax.jcr.lock.LockException  if the <code>save</code> would result in a
         *                                       change to persistent storage that would violate a lock.
         * @throws javax.jcr.nodetype.NoSuchNodeTypeException
         *                                       if the <code>save</code> would result in the
         *                                       addition of a node with an unrecognized node type.
         * @throws javax.jcr.RepositoryException if another error occurs.
         * @deprecated As of JCR 2.0, {@link javax.jcr.Session#save()} should
         *             be used instead.
         */
        public void save()
                throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {

        }

        /**
         * If <code>keepChanges</code> is <code>false</code>, this method discards all pending changes
         * currently recorded in this <code>Session</code> that apply to this Item or any of its descendants
         * (that is, the subtree rooted at this Item)and returns all items to reflect the current
         * saved state. Outside a transaction this state is simple the current state of persistent storage.
         * Within a transaction, this state will reflect persistent storage as modified by changes that have
         * been saved but not yet committed.
         * <p/>
         * If <code>keepChanges</code> is true then pending change are not discarded but items that do not
         * have changes pending have their state refreshed to reflect the current saved state, thus revealing
         * changes made by other sessions.
         * <p/>
         * An <code>InvalidItemStateException</code> is thrown if this <code>Item</code> object represents a
         * workspace item that has been removed (either by this session or another).
         *
         * @param keepChanges a boolean
         * @throws javax.jcr.InvalidItemStateException
         *                                       if this
         *                                       <code>Item</code> object represents a workspace item that has been
         *                                       removed (either by this session or another).
         * @throws javax.jcr.RepositoryException if another error occurs.
         */
        public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {

        }

        /**
         * Removes <code>this</code> item (and its subtree).
         * <p/>
         * To persist a removal, a <code>save</code> must be
         * performed that includes the (former) parent of the
         * removed item within its scope.
         * <p/>
         * If a node with same-name siblings is removed, this decrements by one the
         * indices of all the siblings with indices greater than that of the removed
         * node. In other words, a removal compacts the array of same-name siblings
         * and causes the minimal re-numbering required to maintain the original
         * order but leave no gaps in the numbering.
         * <p/>
         * A <code>ReferentialIntegrityException</code> will be thrown on <code>save</code>
         * if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
         * property located in this workspace but outside this item's subtree and the current <code>Session</code>
         * has read access to that <code>REFERENCE</code> property.
         * <p/>
         * An <code>AccessDeniedException</code> will be thrown on <code>save</code>
         * if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
         * property located in this workspace but outside this item's subtree and the current <code>Session</code>
         * <i>does not</i> have read access to that <code>REFERENCE</code> property or if the current <code>Session</code>
         * does not have sufficient privileges to remove the item.
         * <p/>
         * A <code>ConstraintViolationException</code> will be thrown either immediately
         * or on <code>save</code>, if removing this item would violate a node type or implementation-specific
         * constraint. Implementations may differ on when this validation is performed.
         * <p/>
         * A <code>VersionException</code> will be thrown either immediately
         * or on <code>save</code>, if the parent node of this item is versionable and checked-in
         * or is non-versionable but its nearest versionable ancestor is checked-in. Implementations
         * may differ on when this validation is performed.
         * <p/>
         * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
         * if a lock prevents the removal of this item. Implementations may differ on when this validation is performed.
         *
         * @throws javax.jcr.version.VersionException
         *                                       if the parent node of this item is versionable and checked-in
         *                                       or is non-versionable but its nearest versionable ancestor is checked-in and this
         *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.lock.LockException  if a lock prevents the removal of this item and this
         *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
         * @throws javax.jcr.nodetype.ConstraintViolationException
         *                                       if removing the specified item would violate a node type or
         *                                       implementation-specific constraint and this implementation performs this validation immediately
         *                                       instead of waiting until <code>save</code>.
         * @throws javax.jcr.AccessDeniedException
         *                                       if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
         *                                       property located in this workspace but outside this item's subtree and the current <code>Session</code>
         *                                       <i>does not</i> have read access to that <code>REFERENCE</code> property or if the current <code>Session</code>
         *                                       does not have sufficient privileges to remove the item.
         * @throws javax.jcr.RepositoryException if another error occurs.
         * @see javax.jcr.Session#removeItem(String)
         */
        public void remove()
                throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {

        }

    }

    protected JahiaUser lookupUser() {
        return ServicesRegistry.getInstance().getJahiaUserManagerService()
                .lookupUser(node.getName());
    }
}
