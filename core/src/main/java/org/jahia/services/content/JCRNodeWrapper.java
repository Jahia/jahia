/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.mutable.MutableInt;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.decorator.JCRPlaceholderNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Interface for wrappers around <code>javax.jcr.Node</code> to be able to inject
 * Jahia specific actions.
 * <p/>
 * Jahia services should use this interface rather than the original to ensure that
 * we manipulate wrapped nodes and not the ones from the underlying implementation
 *
 * @author toto
 */
public interface JCRNodeWrapper extends Node, JCRItemWrapper {

    Iterator<JCRNodeWrapper> EMPTY = new Iterator<JCRNodeWrapper>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public JCRNodeWrapper next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Defines ways to deal with duplicate node names when copying a node to a different parent
     * that already contains a node with the same name and same name siblings are not allowed.
     */
    enum NodeNamingConflictResolutionStrategy {
        MERGE,
        FAIL
    }

    /**
     * Gets the real <code>Node</code> wrapped by this <code>JCRNodeWrapper</code>
     *
     * This gives a direct access to the underlying implementation node, all operations done here will bypass the
     * JCRSessionWrapper / JCRNodeWrapper layer. Among other things:
     *
     * <ul>
     * <li>PropertyInterceptor won't be called</li>
     * <li>I18N properties will not be handled</li>
     * <li>Save on underlying session will not call listeners</li>
     * <li>Read-only mode will not be checked.</li>
     * </ul>
     *
     * @return the real JCR <code>Node</code>
     */
    Node getRealNode();

    /**
     * Obtains the <code>JahiaUser</code> object of the current <code>Session</code>, which returned the node
     *
     * @return the user of the current <code>Session</code>
     */
    JCRUserNode getUser();

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return the <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code> set, or <code>null</code>
     * if this method was used to remove a property (by setting its value to <code>null</code>).
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return the <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code> set, or <code>null</code>
     * if this method was used to remove a property (by setting its value to <code>null</code>).
     */
    @Override
    JCRPropertyWrapper setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     * @deprecated As of JCR 2.0, {@link #setProperty(String, javax.jcr.Binary)} should
     * be used instead.
     */
    @Override
    JCRPropertyWrapper setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     */
    @Override
    JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The wrapped node at <code>relPath</code>.
     */
    @Override
    JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException;

    /**
     * Adds the namespace prefix to the specified property name and sets the property to the specified value.
     *
     * @param namespace
     * @param name      The name of a property of this node
     * @param value     The value to assigned
     * @return The updated <code>Property</code> object wrapped in <code>JCRPropertywWrapper</code>
     * @throws RepositoryException If an error occurs.
     * @see #setProperty(String, String)
     */
    JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The wrapped property at <code>relPath</code>.
     */
    @Override
    JCRPropertyWrapper getProperty(String relPath) throws PathNotFoundException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return the wrapped primary child item.
     */
    @Override
    JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return a <code>ExtendedNodeType</code> object.
     */
    @Override
    ExtendedNodeType getPrimaryNodeType() throws RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return an array of  <code>ExtendedNodeType</code> objects.
     */
    @Override
    ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return an <code>ExtendedNodeDefinition</code> object.
     */
    @Override
    ExtendedNodeDefinition getDefinition() throws RepositoryException;

    /**
     * get all the ACL entries including inherited ones
     *
     * @return a map of (user, list(path, GRANT/DENY, permission))
     */
    Map<String, List<String[]>> getAclEntries();

    /**
     * get the active ACL entries
     *
     * @return a map of (user, map(permisson, GRANT/DENY))
     */
    Map<String, Map<String, String>> getActualAclEntries();

    /**
     * Return the list of permission applicable to this node. Permissions are organized by group, result is a map
     * where the key is the group, and value the list of associated permissions.
     *
     * @return list of permissions applicable to this node
     */
    Map<String, List<JCRNodeWrapper>> getAvailableRoles() throws RepositoryException;

    /**
     * Checks if the current user has a permission or not
     *
     * @param perm The permission to check
     * @return If the permission is allowed
     */
    boolean hasPermission(String perm);

    Set<String> getPermissions();

    BitSet getPermissionsAsBitSet();

    /**
     * Grant roles to a principal on this node.
     *
     * @param principalKey the name of a principal with a prefix, such as "u:" for users, "g:" for groups
     * @param roles        the names of roles that this principal should be granted.
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean grantRoles(String principalKey, Set<String> roles) throws RepositoryException;

    /**
     * Deny roles to a principal on this node.
     *
     * @param principalKey the name of a principal with a prefix, such as "u:" for users, "g:" for groups
     * @param roles        the names of roles that this principal should be denied.
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean denyRoles(String principalKey, Set<String> roles) throws RepositoryException;

    /**
     * Change the permissions of a principal on the node.
     *
     * @param principalKey the name of a principal with a prefix, such as "u:" for users, "g:" for groups
     * @param perms        A map with the name of the permission, and "GRANT" or "DENY" as a value
     * @return true if action was successful, or false if not
     */
    boolean changeRoles(String principalKey, Map<String, String> perms) throws RepositoryException;

    /**
     * Revoke all permissions for the specified principal
     *
     * @param principalKey the name of a principal with a prefix, such as "u:" for users, "g:" for groups
     * @return true if action was successful, or false if not
     */
    boolean revokeRolesForPrincipal(String principalKey) throws RepositoryException;

    /**
     * Revoke all permissions for all users
     *
     * @return true if action was successful, or false if not
     */
    boolean revokeAllRoles() throws RepositoryException;

    /**
     * Check if acl inheritance is broken on this node or not
     *
     * @return true if ACL inheritance is broken
     */
    boolean getAclInheritanceBreak() throws RepositoryException;

    /**
     * Set acl break inheritance - if true, no ACLs will be inherited from parent nodes.
     *
     * @param breakAclInheritance if <code>true</code>, no ACLs will be inherited from parent nodes
     * @return <code>true</code> if action was successful, or <code>false</code> if not
     */
    boolean setAclInheritanceBreak(boolean breakAclInheritance) throws RepositoryException;

    /**
     * Create a sub folder of type jnt:folder
     *
     * @param name Name of the folder to create
     * @return The wrapped sub folder node that was added.
     * @throws RepositoryException in case of JCR-related errors
     * @see #addNode(String)
     */
    JCRNodeWrapper createCollection(String name) throws RepositoryException;

    /**
     * Upload a new file under this node
     *
     * @param name        Name of the file node
     * @param is          The input stream with the file's content
     * @param contentType The MIME content type
     * @return The wrapped file node that was uploaded
     * @throws RepositoryException in case of JCR-related errors
     */
    JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException;

    /**
     * Get the absolute file content url
     *
     * @param request the current request.
     * @return the absolute file content url
     */
    String getAbsoluteUrl(ServletRequest request);

    /**
     * Get the file content url
     *
     * @return the file content url
     */
    String getUrl();

    /**
     * Get the file content url with optional params
     *
     * @return the file content url
     */
    String getUrl(List<String> params);

    /**
     * Get the absolute webdav url
     *
     * @return the absolute webdav url
     */
    String getAbsoluteWebdavUrl(final HttpServletRequest request);

    /**
     * Get the webdav url
     *
     * @return the webdav url
     */
    String getWebdavUrl();

    /**
     * Get the list of all available thumbnails. Each result is a subnode the node, of type jnt:resource
     *
     * @return the list of all available thumbnails
     */
    List<String> getThumbnails();

    /**
     * Get the file content url for a specific thumbnail
     *
     * @param name The thumbnail name
     * @return the file content url for a specific thumbnail
     */
    String getThumbnailUrl(String name);

    /**
     * Get a map of all available thumbnails and its URLs.
     *
     * @return a map of all available thumbnails and its URLs.
     */
    Map<String, String> getThumbnailUrls();

    /**
     * Returns a map of all property names of this node accessible through the current <code>Session</code> with the value as String.
     *
     * @return a map of all property names and its String values
     * @throws RepositoryException in case of JCR-related errors
     */
    Map<String, String> getPropertiesAsString() throws RepositoryException;

    /**
     * Returns the name of the primary node type in effect for this node.
     *
     * @return the name of the primary node type
     * @throws RepositoryException in case of JCR-related errors
     */
    String getPrimaryNodeTypeName() throws RepositoryException;

    /**
     * Returns a list holding the primary node type and all mixin node types for this node
     *
     * @return a list holding the primary node type and all mixin node types
     * @throws RepositoryException in case of JCR-related errors
     */
    List<String> getNodeTypes() throws RepositoryException;

    /**
     * Checks whether the current node is a collection node
     *
     * @return true if current node is a collection node
     */
    boolean isCollection();

    /**
     * Checks whether the current node is a file node
     *
     * @return true if current node is a file node
     */
    boolean isFile();

    /**
     * Checks whether the current node is a portlet node
     *
     * @return true if current node is a portlet node
     */
    boolean isPortlet();

    /**
     * Returns the last node modification date as <code>Date</code> object
     *
     * @return the last node modification date as <code>Date</code> object
     */
    Date getLastModifiedAsDate();

    /**
     * Returns the last node published date as <code>Date</code> object
     *
     * @return the last node published date as <code>Date</code> object
     */
    Date getLastPublishedAsDate();

    /**
     * Returns the last content modification date as <code>Date</code> object
     *
     * @return the last content modification date as <code>Date</code> object
     */
    Date getContentLastModifiedAsDate();

    /**
     * Returns the last content published date as <code>Date</code> object
     *
     * @return the last content published date as <code>Date</code> object
     */
    Date getContentLastPublishedAsDate();

    /**
     * Returns the node creation date as <code>Date</code> object
     *
     * @return the node creation date as <code>Date</code> object
     */
    Date getCreationDateAsDate();

    /**
     * Returns the user name who created this node
     *
     * @return the user name who created this node
     */
    String getCreationUser();

    /**
     * Returns the user name who last modified this node
     *
     * @return the user name who last modified this node
     */
    String getModificationUser();

    /**
     * Returns the user name who last published this node
     *
     * @return the user name who last published this node
     */
    String getPublicationUser();

    /**
     * Returns the language code of this node
     *
     * @return the language code of this node
     */
    String getLanguage();

    /**
     * Returns the property value as <code>String</code> at <code>name</code> relative to <code>this</code>
     * node.
     *
     * @param name The relative path of the property to retrieve.
     * @return the property value as <code>String</code>
     */
    String getPropertyAsString(String name);

    /**
     * Returns a list of all the ancestor of this item.
     *
     * @return a list of all the ancestor of this item.
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<JCRItemWrapper> getAncestors() throws RepositoryException;

    /**
     * Rename the current file node
     *
     * @param newName The new name
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean rename(String newName) throws RepositoryException;

    /**
     * Copy the current file node to another destination
     *
     * @param dest The destination name for the file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean copy(String dest) throws RepositoryException;

    /**
     * Copy the current file node to another destination supplying it given name
     *
     * @param dest The destination name for the file node
     * @param name The new name of the copied file node
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean copy(String dest, String name) throws RepositoryException;

    /**
     * Copy the current file node to another destination supplying it given name
     *
     * @param dest The destination name for the file node
     * @param name The new name of the copied file node
     * @param namingConflictResolutionStrategy The way to deal with possible node naming conflict when node with given name already exists while same sibling names are not allowed
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean copy(String dest, String name, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException;

    /**
     * Copy the current file node to another destination node supplying it given name
     *
     * @param node The destination node
     * @param name The new name of the copied file node
     * @param allowsExternalSharedNodes
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean copy(JCRNodeWrapper node, String name, boolean allowsExternalSharedNodes) throws RepositoryException;

    /**
     * Copy the current file node to another destination node supplying it given name
     *
     * @param node The destination node
     * @param name The new name of the copied file node
     * @param allowsExternalSharedNodes
     * @param namingConflictResolutionStrategy The way to deal with possible node naming conflict when node with given name already exists while same sibling names are not allowed
     * @return true if action was successful, or false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean copy(JCRNodeWrapper node, String name, boolean allowsExternalSharedNodes, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException;

    boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references) throws RepositoryException;

    boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, List<String> ignoreNodeTypes, int maxBatch) throws RepositoryException;

    boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batch) throws RepositoryException;

    boolean copy(
        JCRNodeWrapper dest,
        String name,
        boolean allowsExternalSharedNodes,
        Map<String, List<String>> references,
        List<String> ignoreNodeTypes,
        int maxBatch,
        MutableInt batch,
        NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy
    ) throws RepositoryException;

    void copyProperties(JCRNodeWrapper destinationNode, Map<String, List<String>> references) throws RepositoryException;

    /**
     * Get a lock on this node and store the lock token
     *
     * @param type
     * @return true if action was successful, or false if not
     * @see #lock(boolean, boolean)
     */
    boolean lockAndStoreToken(String type) throws RepositoryException;

    boolean lockAndStoreToken(String type, String userID) throws RepositoryException;

    /**
     * Get the name of the user who locked the node
     *
     * @return the name of the user who locked the node
     */
    String getLockOwner() throws RepositoryException;

    Map<String, List<String>> getLockInfos() throws RepositoryException;

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isLocked();

    @Override
    Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    @Override
    Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException;

    @Override
    boolean holdsLock() throws RepositoryException;

    @Override
    void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    void unlock(String type) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    void unlock(String type, String userID) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    void clearAllLocks() throws RepositoryException;

    void checkLock() throws RepositoryException;

    /**
     * Adds the mix:versionable mixin type to a file node
     */
    void versionFile();

    /**
     * Checks whether the current file node has a mix:versionable mixin type applied
     *
     * @return whether current file has mix:versionable
     */
    boolean isVersioned();

    /**
     * Performs a checkin and a checkout.
     *
     * @see #checkin()
     * @see #checkout()
     */
    void checkpoint();

    /**
     * Retrieves the list of versions, in all workspaces, ignoring internal version created in the publication process.
     *
     * @return a List of VersionInfo objects containing the resolved versions, as well as extra information such as the
     * checkinDate if available.
     * @throws RepositoryException happens if there was a problem retrieving the list of versions.
     */
    List<VersionInfo> getVersionInfos() throws RepositoryException;

    /**
     * Retrieves the list of versions, ignoring internal version created in the publication process. This method should
     * be preferred to the getVersions that actually retrieves ALL versions in all workspaces. This one internally calls
     * the getAllLinearVersions.
     *
     * @return a List of VersionInfo objects containing the resolved versions, as well as extra information such as the
     * checkinDate if available.
     * @throws RepositoryException happens if there was a problem retrieving the list of versions.
     */
    List<VersionInfo> getLinearVersionInfos() throws RepositoryException;

    /**
     * Gets a list of all versions of this node
     *
     * @return a list of all versions name of this node
     * @see #getVersionHistory()
     */
    List<String> getVersions();

    /**
     * Gets a list of all versions for this node as Version object
     *
     * @return a list of Version object
     */
    List<Version> getVersionsAsVersion();

    /**
     * The <code>JCRStoreProvider</code> which returned the current node.
     *
     * @return the <code>JCRStoreProvider</code> for the current node
     * @deprecated use {@link #getProvider()} instead
     */
    @Deprecated
    JCRStoreProvider getJCRProvider();

    /**
     * The <code>JCRStoreProvider</code> which returned the current node.
     *
     * @return the <code>JCRStoreProvider</code> for the current node
     */
    JCRStoreProvider getProvider();

    /**
     * The <code>JCRFileContent</code> object of the current file node
     *
     * @return the <code>JCRFileContent</code> object of the current file node
     */
    JCRFileContent getFileContent();

    /**
     * Get the property definition object applicable to the given property name
     *
     * @param propertyName the name of the property to find the corresponding definition
     * @return the <code>ExtendedPropertyDefinition</code> for the given property
     * @throws RepositoryException in case of JCR-related errors
     */
    ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName) throws RepositoryException;

    /**
     * Get the property definition object applicable to the given property name
     *
     * @param propertyName         the name of the property to find the corresponding definition
     * @param requiredPropertyType the type of the property to find the corresponding definition
     * @param isMultiple           is the property multiple
     * @return the <code>ExtendedPropertyDefinition</code> for the given property
     * @throws RepositoryException in case of JCR-related errors
     */
    ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName, int requiredPropertyType, boolean isMultiple) throws RepositoryException;


    List<ExtendedPropertyDefinition> getReferenceProperties() throws RepositoryException;

    /**
     * Get the child node definition object applicable to the given child name
     *
     * @param childName the name of the property to find the corresponding definition
     * @param nodeType
     * @return the <code>ExtendedPropertyDefinition</code> for the given property
     * @throws ConstraintViolationException
     * @throws RepositoryException in case of JCR-related errors
     */
    ExtendedNodeDefinition getApplicableChildNodeDefinition(String childName, String nodeType) throws ConstraintViolationException, RepositoryException;

    /**
     * Check whether the current node is of mixin type mix:lockable
     *
     * @return true if node can be locked
     */
    boolean isLockable();

    public List<Locale> getLockedLocales() throws RepositoryException;

    /**
     * {@inheritDoc}
     *
     * @return The wrapped node that was added.
     */
    @Override
    JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    /**
     * @return The wrapped node that was added.
     * @see Node#addNode(String, String)
     */
    @Override
    JCRNodeWrapper addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException;

    /**
     * @return The wrapped node that was added.
     * @see Node#addNode(String, String)
     */
    JCRNodeWrapper addNode(String relPath, String primaryNodeTypeName, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException;

    /**
     * Return the <code>JCRPlaceholderNode</code> based on the current node
     *
     * @return the <code>JCRPlaceholderNode</code> based on the current node
     * @throws RepositoryException in case of JCR-related errors
     */
    JCRPlaceholderNode getPlaceholder() throws RepositoryException;

    /**
     * {@inheritDoc}
     */
    @Override
    String getName();

    /**
     * {@inheritDoc}
     */
    @Override
    String getPath();

    /**
     * Get the translation node
     * Use only in un-localized session
     *
     * @param locale
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    Node getI18N(Locale locale) throws RepositoryException;

    /**
     * Get the translation node
     * Use only in un-localized session
     *
     * @param locale
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    Node getI18N(Locale locale, boolean fallback) throws RepositoryException;

    /**
     * Returns available translation sub-nodes of this node.
     *
     * @return available translation sub-nodes of this node
     * @throws RepositoryException in case of a JCR exception
     */
    NodeIterator getI18Ns() throws RepositoryException;

    /**
     * Checks if the translation node exists
     * Use only in un-localized session
     *
     * @param locale
     * @return <code>true</code> if the translation node exists
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean hasI18N(Locale locale) throws RepositoryException;

    /**
     * Checks if the translation node exists
     * Use only in un-localized session
     *
     * @param locale   the locale for which we want to check if a translation exists
     * @param fallback whether or not we should fallback on the default locale if we didn't find an exact translation
     * @return <code>true</code> if the translation node exists
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean hasI18N(Locale locale, boolean fallback) throws RepositoryException;

    /**
     * Get or create the translation node
     * Use only in un-localized session
     *
     * @param locale
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    Node getOrCreateI18N(Locale locale) throws RepositoryException;

    Node getOrCreateI18N(Locale locale, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws RepositoryException;

    /**
     * Retrieves the list of locales available on a node.
     *
     * @return a list of the locales that exist on this node.
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<Locale> getExistingLocales() throws RepositoryException;

    JCRNodeWrapper clone(JCRNodeWrapper sharedNode, String name) throws ItemExistsException, VersionException,
            ConstraintViolationException, LockException,
            RepositoryException;

    boolean checkValidity();

    boolean checkLanguageValidity(Set<String> languages);

    JCRSiteNode getResolveSite() throws RepositoryException;

    boolean hasTranslations() throws RepositoryException;

    boolean checkI18nAndMandatoryPropertiesForLocale(Locale locale) throws RepositoryException;

    String getDisplayableName();

    /**
     * Returns an unescaped node local name for displaying.
     *
     * @return an unescaped node local name
     */
    String getUnescapedName();

    AccessControlManager getAccessControlManager() throws RepositoryException;

    /**
     * Checks if this node can be marked for deletion. This will usually return false on repository implementations
     * that do not support versioining or adding mixins, etc...
     *
     * @return true if the node supports marking for deletion.
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean canMarkForDeletion() throws RepositoryException;

    /**
     * Returns true if this node is marked for deletion.
     *
     * @return true if this node is marked for deletion.
     * @throws RepositoryException in case of a repository access error
     */
    boolean isMarkedForDeletion() throws RepositoryException;

    /**
     * Marks this node and all the sub-nodes for deletion. Additionally the deletion info is stored in this node, including the user, date
     * and optional comment. This operation does not call session.save() after modifications.
     *
     * @param comment the deletion comment
     * @throws RepositoryException in case of repository access errors
     */
    void markForDeletion(String comment) throws RepositoryException;

    /**
     * Unmarks this node and all the sub-nodes for deletion. This operation does not call session.save() after modifications.
     *
     * @throws RepositoryException in case of repository access errors
     */
    void unmarkForDeletion() throws RepositoryException;

    @Override
    JCRNodeIteratorWrapper getNodes() throws RepositoryException;

    @Override
    JCRNodeIteratorWrapper getNodes(String namePattern) throws RepositoryException;

    @Override
    JCRNodeIteratorWrapper getNodes(String[] nameGlobs) throws RepositoryException;

    @Override
    JCRNodeIteratorWrapper getSharedSet() throws RepositoryException;

    @Override
    JCRNodeIteratorWrapper merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException;
}
