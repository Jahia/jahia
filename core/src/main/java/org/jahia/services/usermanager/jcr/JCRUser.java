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
package org.jahia.services.usermanager.jcr;

import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.*;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 7 juil. 2009
 */
public class JCRUser implements JahiaUser {
    private transient static Logger logger = Logger.getLogger(JCRUser.class);
    private static final String ROOT_USER_UUID = "b32d306a-6c74-11de-b3ef-001e4fead50b";
    private static final String PROVIDER_NAME = "jcr";
    private final String nodeUuid;
    private final JCRStoreService jcrStoreService;
    static final String J_PASSWORD = "j:password";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    private String name;
    private Properties properties;
    private UserProperties userProperties;

    public JCRUser(String nodeUuid, JCRStoreService jcrStoreService) {
        this.nodeUuid = nodeUuid;
        this.jcrStoreService = jcrStoreService;
    }


    /**
     * Return the unique user's username ( = his user key )
     *
     * @return Return a String representation of the user. In Jahia this
     *         corresponds to the user key, which is unique within a Jahia system.
     */
    public String getName() {
        JCRSessionWrapper session = null;
        try {
            if (name == null) {
                session = jcrStoreService.getSystemSession();
                name = getNode(session).getName();
            }
            return name;
        } catch (RepositoryException e) {
            return "";
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private Node getNode(JCRSessionWrapper session) throws RepositoryException {
        return session.getNodeByUUID(nodeUuid);
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
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */
    public int getHomepageID() {
        return -1;
    }

    /**
     * Set the home page id.
     *
     * @param id The group homepage id.
     * @return false on error
     */
    public boolean setHomepageID(int id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
            JCRSessionWrapper session = null;
            try {
                session = jcrStoreService.getSystemSession();
                PropertyIterator iterator = getNode(session).getProperties();
                for (; iterator.hasNext();) {
                    Property property = iterator.nextProperty();
                    if (!property.getDefinition().isMultiple()) {
                        properties.put(property.getName(), property.getString());
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e);
            } finally {
                if (session != null) {
                    session.logout();
                }
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
            JCRSessionWrapper session = null;
            try {
                session = jcrStoreService.getSystemSession();
                PropertyIterator iterator = getNode(session).getProperties();
                for (; iterator.hasNext();) {
                    Property property = iterator.nextProperty();
                    if (!property.getDefinition().isMultiple()) {
                        userProperties.setUserProperty(property.getName(), new UserProperty(property.getName(),
                                                                                            property.getString(),
                                                                                            false));
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e);
            } finally {
                if (session != null) {
                    session.logout();
                }
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
    public boolean removeProperty(String key) {
        JCRSessionWrapper session = null;
        try {
            session = jcrStoreService.getSystemSession();
            Node node = getNode(session);
            Property property = node.getProperty(key);
            if (property != null) {
                property.remove();
                node.save();
                properties = null;
                userProperties = null;
                return true;
            }
        } catch (RepositoryException e) {
            logger.warn(e);
        } finally {
            if (session != null) {
                session.logout();
            }
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
    public boolean setProperty(String key, String value) {
        JCRSessionWrapper session = null;
        try {
            session = jcrStoreService.getSystemSession();
            Node node = getNode(session);
            node.setProperty(key, value);
            node.save();
            properties = null;
            userProperties = null;
            return true;
        } catch (RepositoryException e) {
            logger.warn(e);
        } finally {
            if (session != null) {
                session.logout();
            }
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
        return setProperty(J_PASSWORD, password);
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
        return isMemberOfGroup(siteID, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
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
     * Verifiy if the passed in password is the same as the encapsulated by this
     * user.
     *
     * @param password String representation of an non-encrypted password.
     * @return Return true if the passed in password is the same as the
     *         encapsulated password in this user, and return false on any error.
     */
    public boolean verifyPassword(String password) {
        return getProperty(J_PASSWORD).equals(JCRUserManagerProvider.encryptPassword(password));
    }

    /**
     * Retrieve a List of language codes stored for this user. The order
     * of these codes is important. The first language is the first choice, and
     * as the list goes down so does the importance of the languages.
     *
     * @return a List containing String objects that contain language codes,
     *         the List may be empty if this property was never set for the user.
     */
    public List<String> getLanguageCodes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the language codes for a user. This List contains String object
     * that contain language codes. The order of this list is defined so as that
     * the most important language is first.
     *
     * @param userLanguages a List of String object containing the language
     *                      codes. The order is from the most important language first to the least
     *                      important one last.
     */
    public void setLanguageCodes(List<String> userLanguages) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns true if this user has activated the language mixing function
     * of Jahia. This means that the content that will be displayed on his
     * page will content multiple languages based on language preference list.
     *
     * @return true if the property is active.
     */
    public boolean isMixLanguagesActive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the user property that indicates whether the user wants to see
     * mixed language content in the pages he is browsing or not.
     *
     * @param mixLanguagesActive a boolean set to true to allow language mixing
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns true if the user has setup this property indicating that he
     * doesn't want to default to any other languages than the ones he has
     * setup in his users settings. This will shortcut defaulting to the
     * browser and site settings.
     *
     * @return true if the user only wants to see the languages he has configured
     *         and never the ones configured in his browser or in the site settings.
     */
    public boolean isUserLanguagesOnlyActive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the value indicating whether a user wants to fallback to browser
     * or site setting languages. If set to true this means we are NOT falling
     * back to the above mentioned settings.
     *
     * @param userLanguagesOnlyActive true means we are not falling back to
     *                                browser or site settings.
     */
    public void setUserLanguagesOnlyActive(boolean userLanguagesOnlyActive) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the name of the provider of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    public boolean isProxied() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setProxied(boolean proxied) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * if true, ignore user aliasing check at ACL level.
     *
     * @return true if option  byPassUserAliasing is set
     */
    public boolean byPassUserAliasing() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * if true, force ignore user aliasing check at ACL level
     *
     * @param bypassUserAliasing set to true to activate option byPassUserAliasing
     */
    public void setByPassUserAliasing(boolean bypassUserAliasing) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNodeUuid() {
        return nodeUuid;
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

        return nodeUuid.equals(jcrUser.nodeUuid);

    }

    @Override
    public int hashCode() {
        return nodeUuid.hashCode();
    }

    @Override
    public String toString() {
        return "JCRUser{" + "nodeUuid='" + nodeUuid + '\'' + "name='" + getName() + '\'' + '}';
    }
}
