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

package org.jahia.services.usermanager.jcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PasswordHistoryEntry;
import org.jahia.services.usermanager.*;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import java.util.List;
import java.util.Properties;

/**
 * Implementation of JahiaUser using the JCR API
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 7 juil. 2009
 */
public class JCRUser implements JahiaUser, JCRPrincipal {
    private static final long serialVersionUID = 4032549320399420578L;
	private transient static Logger logger = LoggerFactory.getLogger(JCRUser.class);
    protected static final String ROOT_USER_UUID = "b32d306a-6c74-11de-b3ef-001e4fead50b";
    private static final String PROVIDER_NAME = "jcr";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";
    private final String nodeUuid;
    static final String J_PASSWORD = "j:password";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    private String name;
    private Properties properties;
    private UserProperties userProperties;
    private boolean external;
	private List<PasswordHistoryEntry> passwordHistory;
    private String path = null;

    public JCRUser(String nodeUuid) {
        this(nodeUuid, false);
    }

    public JCRUser(String nodeUuid, boolean isExternal) {
        super();
        this.nodeUuid = nodeUuid;
        this.external = isExternal;
    }


    /**
     * Return the unique user's username ( = his user key )
     *
     * @return Return a String representation of the user. In Jahia this
     *         corresponds to the user key, which is unique within a Jahia system.
     */
    public String getName() {
        if (name == null) {
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
                    public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        name = getNode(session).getName();
                        return name;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while retrieving the user's name", e);
                return "";
            }
        } else {
            return name;
        }
    }

    public JCRNodeWrapper getNode(JCRSessionWrapper session) throws RepositoryException {
        return session.getNodeByIdentifier(getIdentifier());
    }

    /**
     * Return the user username.
     *
     * @return Return the username.
     */
    public String getUsername() {
        return getName();
    }

    /**
     * Return the unique String identifier of this user.
     *
     * @return the unique String identifier of this user.
     */
    public String getUserKey() {
        return "{jcr}" + getName();
    }

    /**
     * Get user's properties list.
     * The properties here should not be modified, as the modifications will
     * not be serialized. Use the setProperty() method to modify a user's
     * properties.
     *
     * @return Return a reference on the user's properties list, or null if no
     *         property is present.
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Properties>() {
                    public Properties doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRUserNode jcrUserNode = (JCRUserNode) getNode(session);
                        PropertyIterator iterator = jcrUserNode.getProperties();
                        for (; iterator.hasNext();) {
                            Property property = iterator.nextProperty();
                            if (!property.getDefinition().isMultiple()) {
                                properties.put(property.getName(), property.getString());
                            } else if (property.getType() == PropertyType.UNDEFINED) {
                                Value[] vals = null;
                                try {
                                    vals = property.getValues();
                                } catch (ValueFormatException e) {
                                    try {
                                        vals = new Value[] { property.getValue() };
                                    } catch (ValueFormatException e1) {
                                        // ignore
                                    }
                                }
                                if (vals != null && vals.length == 1) {
                                    properties.put(property.getName(), vals[0].getString());
                                }
                            }
                        }
                        return properties;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while retrieving properties",e);
            }
        }
        return properties;
    }

    /**
     * Returns the users properties format, in which we can test if a property
     * is read-only or not.
     * The properties here should not be modified, as the modifications will
     * not be serialized. Use the setProperty() method to modify a user's
     * properties.
     *
     * @return UserProperties
     */
    public UserProperties getUserProperties() {
        if (userProperties == null) {
            userProperties = new UserProperties();
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<UserProperties>() {
                    public UserProperties doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRUserNode jcrUserNode = (JCRUserNode) getNode(session);
                        PropertyIterator iterator = jcrUserNode.getProperties();
                        for (; iterator.hasNext();) {
                            Property property = iterator.nextProperty();
                            if(property instanceof JCRUserNode.JCRUserProperty) {
                                userProperties.setUserProperty(property.getName(), new UserProperty(property.getName(),
                                                                                                    property.getString(),
                                                                                                    true));
                            } else if (property.getDefinition() != null && !property.getDefinition().isMultiple()) {
                                userProperties.setUserProperty(property.getName(), new UserProperty(property.getName(),
                                                                                                    property.getString(),
                                                                                                    false));
                            }
                        }
                        if (jcrUserNode.getDisplayableName() != null) {
                            userProperties.setUserProperty(J_DISPLAYABLE_NAME, new UserProperty(J_DISPLAYABLE_NAME, jcrUserNode.getDisplayableName(), true));
                        }
                        return userProperties;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while retrieving user properties", e);
            }
        }
        return userProperties;
    }

    /**
     * Retrieve the requested user property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     *         property does not exist.
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * Retrieves the user property object, so that we can test is the property
     * is read-only or not.
     *
     * @param key String
     * @return UserProperty
     */
    public UserProperty getUserProperty(String key) {
        return getUserProperties().getUserProperty(key);
    }

    /**
     * Remove the specified property from the properties list.
     * This method can fail and do no modification is the property we are
     * trying to set is read-only.
     *
     * @param key Property's name.
     * @return true if everything went well
     */
    public boolean removeProperty(final String key) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    Property property = node.getProperty(key);
                    if (property != null) {
                        session.checkout(node);
                        property.remove();
                        session.save();
                        properties = null;
                        userProperties = null;
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while removing property", e);
        }
        return false;
    }

    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the user's properties list.
     * This method can fail and do no modification is the property we are
     * trying to set is read-only.
     *
     * @param key   Property's name.
     * @param value Property's value.
     * @return true if everything went well
     */
    public boolean setProperty(final String key, final String value) {
        try {
            if (J_EXTERNAL.equals(key)) {
                external = Boolean.valueOf(value);
            }
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    session.checkout(node);
                    node.setProperty(key, value);
                    session.save();
                    properties = null;
                    userProperties = null;
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while setting user property " + key + " with value " + value + " for user " + name, e);
        }
        return false;
    }

    /**
     * Method to test if the password is read-only
     *
     * @return boolean
     */
    public boolean isPasswordReadOnly() {
        return false;
    }

    /**
     * Change the user's password.
     *
     * @param password New user's password
     * @return Return true if the password was successfully changed, false
     *         otherwise
     */
    public boolean setPassword(String password) {
        return setProperty(J_PASSWORD, JahiaUserManagerService.encryptPassword(password));
    }

    /**
     * Test if the user is member of the specified group.
     *
     * @param name   Groupname.
     * @param siteID the site id
     * @return Return true if the user is member of the specified group, or
     *         false on any error.
     */
    public boolean isMemberOfGroup(int siteID, String name) {
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(name)) {
            return true;
        }
        if (JahiaGroupManagerService.USERS_GROUPNAME.equals(name)) {
            return !JahiaUserManagerService.GUEST_USERNAME.equals(getName());
        }
        if (isRoot() && JahiaGroupManagerService.POWERFUL_GROUPS.contains(name)) {
            return true;
        }
        
        // Get the services registry
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        if (servicesRegistry != null) {

            // get the group management service
            JahiaGroupManagerService groupService = servicesRegistry.getJahiaGroupManagerService();

            // lookup the requested group
            JahiaGroup group = groupService.lookupGroup(siteID, name);
            if (group != null) {
                return group.isMember(this);
            }
        }
        return false;
    }

    /**
     * Test if the user is an admin member
     *
     * @param siteID the site id
     * @return Return true if the user is an admin member
     *         false on any error.
     */
    public boolean isAdminMember(int siteID) {
        return isRoot() || isMemberOfGroup(siteID, siteID == 0 ? JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Test if the user is the root user
     *
     * @return Return true if the user is the root user
     *         false on any error.
     */
    public boolean isRoot() {
        return nodeUuid.equals(ROOT_USER_UUID);
    }

    /**
     * Verify if the passed in password is the same as the encapsulated by this
     * user.
     *
     * @param password String representation of an non-encrypted password.
     * @return Return true if the passed in password is the same as the
     *         encapsulated password in this user, and return false on any error.
     */
    public boolean verifyPassword(String password) {
        return getProperty(J_PASSWORD).equals(JahiaUserManagerService.encryptPassword(password));
    }

    /**
     * Get the name of the provider of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Get the path of this user in the local store. For examle for LDAP user this will return the path of
     * the user in the JCR with all necessary encoding.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getLocalPath() {
        if (path == null) {
            path = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(getUsername());
        }
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCRUser jcrUser = (JCRUser) o;

        return getIdentifier().equals(jcrUser.getIdentifier());

    }

    @Override
    public int hashCode() {
        return nodeUuid.hashCode();
    }

    @Override
    public String toString() {
        return "JCRUser{" + "nodeUuid='" + nodeUuid + '\'' + "name='" + getName() + '\'' + '}';
    }

    public String getIdentifier() {
        return nodeUuid;
    }

    /**
     * @return the external
     */
    public boolean isExternal() {
        return external;
    }

	/**
	 * Returns the (encrypted) password history map, sorted by change date
	 * descending, i.e. the newer passwords are at the top of the list.
	 * 
	 * @return the (encrypted) password history list, sorted by change date
	 *         descending, i.e. the newer passwords are at the top of the list
	 */
	public List<PasswordHistoryEntry> getPasswordHistory() {
		if (passwordHistory == null) {
			passwordHistory = JahiaPasswordPolicyService.getInstance().getPasswordHistory(this);
		}

		return passwordHistory;
	}

	/**
	 * Gets the timestamp of the last password change if available. Otherwise
	 * returns <code>0</code>.
	 * 
	 * @return the timestamp of the last password change if available. Otherwise
	 *         returns <code>0</code>
	 */
	public long getLastPasswordChangeTimestamp() {
		List<PasswordHistoryEntry> pwdHistory = getPasswordHistory();

		return pwdHistory.size() > 0 ? pwdHistory.get(0).getModificationDate().getTime() : 0;
	}

    public boolean isAccountLocked() {
        return !isRoot() && Boolean.valueOf(getProperty("j:accountLocked"));
    }
}
