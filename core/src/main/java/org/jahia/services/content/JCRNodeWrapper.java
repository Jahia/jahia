/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.decorator.JCRPlaceholderNode;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * Interface for wrappers around <code>javax.jcr.Node</code> to be able to inject
 * Jahia specific actions. 
 * 
 * Jahia services should use this interface rather than the original to ensure that 
 * we manipulate wrapped nodes and not the ones from the underlying implementation
 *
 * @author toto
 */
public interface JCRNodeWrapper extends Node, JCRItemWrapper {
    /** The OK. */
    public static final int OK = 0;
    /** The INVALID_FILE. */
    public static final int INVALID_FILE = 1;
    /** The ACCESS_DENIED. */
    public static final int ACCESS_DENIED = 2;
    /** The UNSUPPORTED_ERROR. */
    public static final int UNSUPPORTED_ERROR = 98;
    /** The UNKNOWN_ERROR. */
    public static final int UNKNOWN_ERROR = 99;

    /** The READ. */
    public static final String READ = Constants.JCR_READ_RIGHTS;
    /** The WRITE. */
    public static final String WRITE = Constants.JCR_WRITE_RIGHTS;
    /** The READ_LIVE. */
    public static final String READ_LIVE = Constants.JCR_READ_RIGHTS_LIVE;
    /** The WRITE_LIVE. */
    public static final String WRITE_LIVE = Constants.JCR_WRITE_RIGHTS_LIVE;
    /** The MODIFY_ACL. */
    public static final String MODIFY_ACL = Constants.JCR_MODIFYACCESSCONTROL_RIGHTS;

    /** The UNSET. */
    public static final int UNSET = 0;
    /** The GRANTED. */
    public static final int GRANTED = 1;
    /** The DENIED. */
    public static final int DENIED = 2;
    /** The INHERITED. */
    public static final int INHERITED = 4;
    /** The SET. */
    public static final int SET = 8;

    /** The JNT_FILE. */
    public static final String JNT_FILE = Constants.JAHIANT_FILE;
    /** The JNT_FOLDER. */
    public static final String JNT_FOLDER = Constants.JAHIANT_FOLDER;

    /**
     * Gets the real <code>Node</code> wrapped by this <code>JCRNodeWrapper</code>
     * @return the real JCR <code>Node</code>
     */
    Node getRealNode();

    /**
     * Obtains the <code>JahiaUser</code> object of the current <code>Session</code>, which returned the node 
     * @return the user of the current <code>Session</code>
     */
    JahiaUser getUser();

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return the <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code> set, or <code>null</code>
     *         if this method was used to remove a property (by setting its value to <code>null</code>).
     */
    JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return the <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code> set, or <code>null</code>
     *         if this method was used to remove a property (by setting its value to <code>null</code>).
     */
    JCRPropertyWrapper setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     * @deprecated As of JCR 2.0, {@link #setProperty(String, javax.jcr.Binary)} should
     *             be used instead.
     */
    JCRPropertyWrapper setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc} 
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The wrapped node at <code>relPath</code>.
     */
    JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException;
    
    /**
     * Adds the namespace prefix to the specified property name and sets the property to the specified value.
     * 
     * @see #setProperty(String, String)
     * 
     * @param namespace 
     * @param name  The name of a property of this node
     * @param value The value to assigned 
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     * @throws RepositoryException          If an error occurs. 
     */
    JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException;

    /**
     * {@inheritDoc}
     * @return The wrapped property at <code>relPath</code>.
     */
    JCRPropertyWrapper getProperty(String relPath) throws PathNotFoundException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return the wrapped primary child item.
     */
    JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return a <code>ExtendedNodeType</code> object.
     */
    ExtendedNodeType getPrimaryNodeType() throws RepositoryException;

    /**
     * {@inheritDoc}
     * @return an array of  <code>ExtendedNodeType</code> objects.
     */
    ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException;

    /**
     * {@inheritDoc}
     * @return an <code>ExtendedNodeDefinition</code> object.
     */
    ExtendedNodeDefinition getDefinition() throws RepositoryException;

    /**
     * get all the ACL entries including inherited ones
     * @return a map of (user, list(path, GRANT/DENY, permission))
     */
    Map<String, List<String[]>> getAclEntries();

    /**
     * get the active ACL entries
     * @return a map of (user, map(permisson, GRANT/DENY))
     */
    Map<String, Map<String, String>> getActualAclEntries();

    /**
     * Return the list of permission applicable to this node. Permissions are organized by group, result is a map
     * where the key is the group, and value the list of associated permissions.
     *
     * @return list of permissions applicable to this node
     */
    Map<String, List<String>> getAvailablePermissions();

    /**
     * Return whether the node is writeable by the current user or not
     * @return true if the node is writable by current user or false if not
     */
    boolean isWriteable();

    /**
     * Checks if the current user has a permission or not
     * @param perm The permission to check
     * @return If the permission is allowed
     */
    boolean hasPermission(String perm);

    /**
     * Change the permissions of a user on the node.
     * @param user The user to update
     * @param perm the permission to update for the user
     * @return true if action was successful, or false if not
     */
    boolean changePermissions (String user, String perm);

    /**
     * Change the permissions of a user on the node.
     * @param user The user to update
     * @param perms A map with the name of the permission, and "GRANT" or "DENY" as a value
     * @return true if action was successful, or false if not
     */
    boolean changePermissions (String user, Map<String,String> perms);

    /**
     * Revoke all permissions for the specified user
     * @param user
     * @return true if action was successful, or false if not
     */
    boolean revokePermissions (String user);

    /**
     * Revoke all permissions for all users
     * @return true if action was successful, or false if not
     */
    boolean revokeAllPermissions ();

    /**
     * Check if acl inheritance is broken on this node or not
     * @return true if ACL inheritance is broken
     */
    boolean getAclInheritanceBreak();

    /**
     * Set acl break inheritance - if true, no acls will be inherited from parent nodes.
     * @param inheritance
     * @return true if action was successful, or false if not
     */
    boolean setAclInheritanceBreak(boolean inheritance);

    /**
     * Create a sub folder of type jnt:folder
     * @see #addNode(String)
     * @param name Name of the folder to create
     * @return The wrapped sub folder node that was added. 
     * @throws RepositoryException
     */
    JCRNodeWrapper createCollection (String name) throws RepositoryException;

    /**
     * Upload a new file under this node
     * @param name Name of the file node
     * @param is The input stream with the file's content
     * @param contentType The MIME content type
     * @return The wrapped file node that was uploaded 
     * @throws RepositoryException
     */
    JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException;

    /**
     * Get the <code>JahiaFileField</code> object to be used with legacy jahia file fields.
     * @return the Jahia file field object for legacy code
     * @deprecated
     */
    JahiaFileField getJahiaFileField ();

    /**
     * Get the value to store in jahia_fields_data when used as a jahia file field
     * @return the value for jahia_fields_data for legacy code
     * @deprecated
     */
    String getStorageName();

    /**
     * Get the absolute file content url
     *
     * @param jParams The Jahia context parameters
     * @return the absolute file content url
     */
    String getAbsoluteUrl(ParamBean jParams);

    /**
     * Get the file content url
     *
     * @return the file content url
     */
    String getUrl();

    /**
     * Get the absolute webdav url
     *
     * @return the absolute webdav url
     */
    String getAbsoluteWebdavUrl(ParamBean jParams);

    /**
     * Get the webdav url
     *
     * @return the webdav url
     */
    String getWebdavUrl();

    /**
     * Get the list of all available thumbnails. Each result is a subnode the node, of type jnt:extraResource
     * @return the list of all available thumbnails
     */
    List<String> getThumbnails();

    /**
     * Get the file content url for a specific thumbnail
     * @param name The thumbnail name
     * @return the file content url for a specific thumbnail
     */
    String getThumbnailUrl(String name);

    /**
     * Get a map of all available thumbnails and its URLs.
     * @return a map of all available thumbnails and its URLs.
     */
    Map<String,String> getThumbnailUrls();

    /**
     * @see #getNodes()
     * @deprecated Use getNodes()
     */
    List<JCRNodeWrapper> getChildren();

    /**
     * Get all child nodes and a JCRPlaceHolderNode holden the current node
     * @return a list with all child nodes and a JCRPlaceHolderNode holden the current node
     */
    List<JCRNodeWrapper> getEditableChildren();

    /**
     * Check the j:hidden property which controls whether the node is visible or not
     * @return true if the node is visible
     */
    boolean isVisible();

    /**
     * Returns a map of all property names of this node accessible through the current <code>Session</code> with the value as String.
     * @return a map of all property names and its String values
     * @throws RepositoryException
     */
    Map<String, String> getPropertiesAsString() throws RepositoryException;

    /**
     * Returns the name of the primary node type in effect for this node.
     * @return the name of the primary node type
     * @throws RepositoryException
     */
    String getPrimaryNodeTypeName() throws RepositoryException;

    /**
     * Returns a list holding the primary node type and all mixin node types for this node
     * @return a list holding the primary node type and all mixin node types
     * @throws RepositoryException
     */
    List<String> getNodeTypes() throws RepositoryException;

    /**
     * Checks whether the current node is a collection node
     * @return true if current node is a collection node
     */
    boolean isCollection();

    /**
     * Checks whether the current node is a file node
     * @return true if current node is a file node
     */
    boolean isFile();

    /**
     * Checks whether the current node is a portlet node
     * @return true if current node is a portlet node
     */
    boolean isPortlet();

    /**
     * Returns the last node modification date as <code>Date</code> object
     * @return the last node modification date as <code>Date</code> object
     */
    Date getLastModifiedAsDate();

    /**
     * Returns the last node published date as <code>Date</code> object
     * @return the last node published date as <code>Date</code> object
     */
    Date getLastPublishedAsDate();

    /**
     * Returns the last content modification date as <code>Date</code> object
     * @return the last content modification date as <code>Date</code> object
     */
    Date getContentLastModifiedAsDate();

    /**
     * Returns the last content published date as <code>Date</code> object
     * @return the last content published date as <code>Date</code> object
     */
    Date getContentLastPublishedAsDate();

    /**
     * Returns the node creation date as <code>Date</code> object
     * @return the node creation date as <code>Date</code> object
     */
    Date getCreationDateAsDate();

    /**
     * Returns the user name who created this node
     * @return the user name who created this node
     */
    String getCreationUser();

    /**
     * Returns the user name who last modified this node
     * @return the user name who last modified this node
     */
    String getModificationUser();

    /**
     * Returns the user name who last published this node
     * @return the user name who last published this node
     */
    String getPublicationUser();

    /**
     * Returns the property value as <code>String</code> at <code>name</code> relative to <code>this</code>
     * node. 
     * @param name The relative path of the property to retrieve.
     * @return the property value as <code>String</code>
     */
    String getPropertyAsString(String name);

    /**
     * Returns a list of all the ancestor of this item.
     * @return a list of all the ancestor of this item.
     * @throws RepositoryException
     */
    public List<JCRItemWrapper> getAncestors() throws RepositoryException;

    /**
     * Rename the current file node
     * @param newName The new name
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean renameFile(String newName) throws RepositoryException;

    /**
     * Move the current file node to a new destination
     * @param dest The destination name for the file node 
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean moveFile(String dest) throws RepositoryException;

    /**
     * Move the current file node to a new destination and rename it
     * @param dest The destination name for the file node
     * @param name The new name of the file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean moveFile(String dest, String name) throws RepositoryException;

    /**
     * Copy the current file node to another destination
     * @param dest The destination name for the file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean copyFile(String dest) throws RepositoryException;

    /**
     * Copy the current file node to another destination and name it differently
     * @param dest The destination name for the file node
     * @param name The new name of the copied file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean copyFile(String dest, String name) throws RepositoryException;

    /**
     * Copy the current file node to another destination node and name it differently
     * @param node The destination node
     * @param name The new name of the copied file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException
     */
    boolean copyFile(JCRNodeWrapper node, String name) throws RepositoryException;

    /**
     * Get a lock on the system session and store the lock token
     * @return true if lock was successfully set
     */
    boolean lockAsSystemAndStoreToken();

    /**
     * Get a lock on this node and store the lock token
     * @see #lock(boolean, boolean)
     * @return true if action was successful, or false if not
     */
    boolean lockAndStoreToken();

    /**
     * Force an unlock on this node and remove the lock token if one existed
     * @return true if node was unlocked
     */
    boolean forceUnlock();

    /**
     * Get the name of the user who locked the node
     * @return the name of the user who locked the node
     */
    String getLockOwner();

    /**
     * {@inheritDoc}
     */
    boolean isLocked();    
    
    /**
     * Adds the mix:versionable mixin type to a file node
     */
    void versionFile();

    /**
     * Checks whether the current file node has a mix:versionable mixin type applied
     * @return whether current file has mix:versionable
     */
    boolean isVersioned();

    /**
     * Performs a checkin and a checkout.
     * @see #checkin()
     * @see #checkout()
     */
    void checkpoint();

    /**
     * Gets a list of all versions of this node
     * @see #getVersionHistory()
     * @return a list of all versions of this node
     */
    List<String> getVersions();

    /**
     * Gets the frozen version node of a given version name
     * @param name a version name
     * @return the wrapped frozen version node
     */
    JCRNodeWrapper getFrozenVersion(String name);

    /**
     * The <code>JCRStoreProvider</code> which returned the current node. 
     * @return the <code>JCRStoreProvider</code> for the current node 
     */
    JCRStoreProvider getJCRProvider();

    /**
     * The <code>JCRStoreProvider</code> which returned the current node. 
     * @return the <code>JCRStoreProvider</code> for the current node
     */
    JCRStoreProvider getProvider();

    /**
     * The <code>JCRFileContent</code> object of the current file node  
     * @return the <code>JCRFileContent</code> object of the current file node
     */
    JCRFileContent getFileContent();

    /**
     * Return a list of <code>UsageEntry</code> objects, to get all cross references of the current node. 
     * @return a list of <code>UsageEntry</code> objects for cross references of this node
     */
    List<UsageEntry> findUsages();

    /**
     * Return a list of <code>UsageEntry</code> objects, to get all cross references of the current node 
     * with the ability to specify that only locked usages should be returned.
     * @param onlyLocked set to true if only locked usages should be returned
     * @return a list of <code>UsageEntry</code> objects for cross references of this node
     */
    List<UsageEntry> findUsages(boolean onlyLocked);

    /**
     * Return a list of <code>UsageEntry</code> objects, to get all cross references of the current node 
     * with the ability to specify that only locked usages should be returned.
     * @param context The Jahia processing context to be passed to the UsageEntry objects
     * @param onlyLocked set to true if only locked usages should be returned
     * @return a list of <code>UsageEntry</code> objects for cross references of this node
     */
    List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked);

    /**
     * Return a list of <code>UsageEntry</code> objects, to get all cross references of the current node 
     * with the ability to specify that only locked usages should be returned.
     * @param context The Jahia processing context to be passed to the UsageEntry objects
     * @param onlyLocked set to true if only locked usages should be returned
     * @param versionName the version name passed to the UsageEntry objects
     * @return a list of <code>UsageEntry</code> objects for cross references of this node
     */
    List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked, String versionName);

    /**
     * Get the property definition object applicable to the given property name 
     * @param propertyName the name of the property to find the corresponding definition
     * @return the <code>ExtendedPropertyDefinition</code> for the given property
     * @throws ConstraintViolationException 
     * @throws RepositoryException
     */
    ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName) throws ConstraintViolationException, RepositoryException ;

    /**
     * Check whether the current node is of mixin type mix:lockable
     * @return true if node can be locked
     */
    boolean isLockable();

    /**
     * {@inheritDoc}
     * @return The wrapped node that was added. 
     */
    JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The wrapped node that was added. 
     */
    JCRNodeWrapper addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException;

    /**
     * Return the <code>JCRPlaceholderNode</code> based on the current node
     * @return the <code>JCRPlaceholderNode</code> based on the current node
     * @throws RepositoryException
     */
    JCRPlaceholderNode getPlaceholder() throws RepositoryException;
    
    /**
     * {@inheritDoc}
     */
    String getName();
    
    /**
     * {@inheritDoc}
     */
    String getPath();         
}
