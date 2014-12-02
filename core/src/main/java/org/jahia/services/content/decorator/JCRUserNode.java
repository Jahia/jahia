/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.content.decorator;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PasswordHistoryEntry;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserImpl;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent a user JCR node.
 *
 * @author rincevent
 */
public class JCRUserNode extends JCRNodeDecorator {
    private transient static Logger logger = LoggerFactory.getLogger(JCRUserNode.class);
    public static final String ROOT_USER_UUID = "b32d306a-6c74-11de-b3ef-001e4fead50b";
    public static final String PROVIDER_NAME = "jcr";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";
    public static final String J_PASSWORD = "j:password";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    private static final String J_PUBLIC_PROPERTIES = "j:publicProperties";
    public final List<String> publicProperties = Arrays.asList(J_EXTERNAL, J_EXTERNAL_SOURCE, J_PUBLIC_PROPERTIES);

    public JCRUserNode(JCRNodeWrapper node) {
        super(node);
    }

    public JahiaUser getJahiaUser() {
        Properties properties = new Properties();
        try {
            properties.putAll(getPropertiesAsString());
        } catch (RepositoryException e) {
            logger.error("Cannot read user properties",e);
        }
<<<<<<< .working
        return new JahiaUserImpl(getName(), getPath(), properties, isRoot(), getProviderName(), getRealm());
=======
        if (!canGetProperty(s)) {
            throw new PathNotFoundException(s);
        }
        if (user == null) {
            user = lookupUser();
        }
        if (user == null || user instanceof JCRUser) {
            return super.getProperty(s);
        } else {
            boolean isExternal = user instanceof JahiaExternalUser;
            String property = isExternal ? ((JahiaExternalUser) user).getExternalProperties().getProperty(s) : user.getProperty(s);
            if (property == null && isExternal) {
                property = user.getUserProperties().getProperty(s);
            }
            if (null == property) {
                return super.getProperty(s);
            }
            ExtendedPropertyDefinition def = propertyDefinitionMap.get(s);
            if (def == null) {
                def = unstructuredPropertyDefinitions.get(PropertyType.STRING);
            }
            return new JCRPropertyWrapperImpl(node, new JCRUserProperty(s, property, def.getRequiredType()),
                    node.getSession(), node.getJCRProvider(), def);
        }
    }
>>>>>>> .merge-right.r51590

<<<<<<< .working
=======
    @Override
    public boolean hasProperty(String s) throws RepositoryException {
        if (user == null) {
            user = lookupUser();
        }
        if (user == null || user instanceof JCRUser) {
            boolean b = super.hasProperty(s);
            return b && canGetProperty(s);
        } else {
            boolean isExternal = user instanceof JahiaExternalUser;
            String property = isExternal ? ((JahiaExternalUser) user).getExternalProperties().getProperty(s) : user.getProperty(s);
            if(property==null && isExternal) {
                property = user.getUserProperties().getProperty(s);
            }
            if (property == null) {
                // actually read the property on the corresponding JCR node
                return super.hasProperty(s) && canGetProperty(s);
            }
            return canGetProperty(s);
        }
>>>>>>> .merge-right.r51590
    }

    /**
     * @deprecated 
     */
    public String getUsername() {
        return getName();
    }

    public String getUserKey() {
        return getPath();
    }

<<<<<<< .working
    public String getProviderName() {
        return getProvider().getKey();
=======
    @Override
    public JCRPropertyWrapper setProperty(String s, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(s,  value, PropertyType.STRING);
    }
    
    @Override
    public JCRPropertyWrapper setProperty(String s, String value, int type) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        if (JCRUser.J_EXTERNAL.equals(s) || Constants.CHECKIN_DATE.equals(s)) {
            return super.setProperty(s, value, type);
        }
        if (user == null) {
            user = lookupUser();
        }
        if (user == null || user instanceof JCRUser) {
            return super.setProperty(s, value, type);
        } else {
            if (user instanceof JahiaExternalUser
                    && ((JahiaExternalUser) user).getExternalProperties().hasProperty(s)
                    || !(user instanceof JahiaExternalUser) && user.getProperty(s) != null) {
                throw new AccessDeniedException("Cannot update external property");
            }
            JCRPropertyWrapper prop = super.setProperty(s, value, type);
            try {
                user.getUserProperties().setProperty(s, value);
            } catch (UserPropertyReadOnlyException e) {
                logger.warn("Cannot set read-only property {} for user {}", s, user.getUserKey());

            }
            return prop;
        }
    }

    @Override
    public JCRPropertyWrapper setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
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
            JCRPropertyWrapper prop = super.setProperty(s, value);
            try {
                user.getUserProperties().setProperty(s, value.getString());
            } catch (UserPropertyReadOnlyException e) {
                logger.warn("Cannot set read-only property {} for user {}", s, user.getUserKey());

            }
            return prop;
        }
>>>>>>> .merge-right.r51590
    }

    public boolean isPropertyEditable(String name) {
        try {
            return !(J_EXTERNAL.equals(name) || Constants.CHECKIN_DATE.equals(name)) && canGetProperty(name);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isRoot() {
        try {
            return getIdentifier().equals(JCRUserNode.ROOT_USER_UUID);
        } catch (RepositoryException e) {
            return false;
        }
    }

    private boolean canGetProperty(String s) throws RepositoryException {
        if (publicProperties.contains(s) || hasPermission("jcr:write")) {
            return true;
        }
        if (!super.hasProperty(J_PUBLIC_PROPERTIES)) {
            return false;
        }
        Property p = super.getProperty(J_PUBLIC_PROPERTIES);
        Value[] values = p.getValues();
        for (Value value : values) {
            if (s.equals(value.getString())) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyPassword(String userPassword) {
        try {
            return StringUtils.isNotEmpty(userPassword) && JahiaUserManagerService.encryptPassword(userPassword).equals(getProperty(J_PASSWORD).getString());
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean setPassword(String pwd) {
        try {
            setProperty(J_PASSWORD, JahiaUserManagerService.encryptPassword(pwd));
            return true;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

<<<<<<< .working
    public boolean isAccountLocked() {
        try {
            return !isRoot() && hasProperty("j:accountLocked") && getProperty("j:accountLocked").getBoolean();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
=======
    public class JCRUserProperty implements Property {
        private final String key;
        private final Object value;
        private int type;

        public JCRUserProperty(String key, Object value) {
            this(key, value, PropertyType.STRING);
        }

        public JCRUserProperty(String key, Object value, int type) {
            super();
            this.key = key;
            this.value = value;
            this.type = type;
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
            return new ValueImpl(value.toString(), type);
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
            throw new ValueFormatException();
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
            return new ByteArrayInputStream(value.toString().getBytes());
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
            try {
                return new BinaryImpl(new ByteArrayInputStream(value.toString().getBytes()));
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
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
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                throw new ValueFormatException(e);
            }
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
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new ValueFormatException(e);
            }
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
            return new BigDecimal(value.toString());
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
            Calendar calendar = ISO8601.parse(value.toString());
            if (calendar == null) {
                throw new ValueFormatException();
            }
            return calendar;
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
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (NumberFormatException e) {
                throw new ValueFormatException(e);
            }
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
            throw new ValueFormatException();
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
            throw new ValueFormatException();
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
            return value.toString().length();
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
            throw new ValueFormatException();
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
            throw new RepositoryException("Cannot get property definition");
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
            return type;
        }

        public boolean isMultiple() throws RepositoryException {
>>>>>>> .merge-right.r51590
            return false;
        }
    }

    public List<PasswordHistoryEntry> getPasswordHistory() {
        return JahiaPasswordPolicyService.getInstance().getPasswordHistory(this);
    }

    public long getLastPasswordChangeTimestamp() {
        List<PasswordHistoryEntry> pwdHistory = getPasswordHistory();

        return pwdHistory.size() > 0 ? pwdHistory.get(0).getModificationDate().getTime() : 0;
    }

    public boolean isMemberOfGroup(String siteKey, String name) {
        return JahiaGroupManagerService.GUEST_GROUPNAME.equals(name) ||
                JahiaGroupManagerService.USERS_GROUPNAME.equals(name) ||
                (JahiaGroupManagerService.SITE_USERS_GROUPNAME.equals(name) && (getRealm() == null || getRealm().equals(siteKey))) ||
                (isRoot() && JahiaGroupManagerService.POWERFUL_GROUPS.contains(name)) ||
                JahiaGroupManagerService.getInstance().isMember(getName(), getRealm(), name, siteKey);
    }

    public String getRealm() {
        return getPath().startsWith("/sites/") ? StringUtils.substringBetween(getPath(), "/sites/", "/") : null;
    }

    /**
     * @deprecated for compatibility only, use getPath()
     */
    public String getLocalPath() {
        return getPath();
    }
}
