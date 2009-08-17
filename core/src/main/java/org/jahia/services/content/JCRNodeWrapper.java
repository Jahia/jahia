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
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * User: toto
 * Date: 22 nov. 2007 - 18:21:08
 */
public interface JCRNodeWrapper extends Node, JCRItemWrapper {
    public static final int OK = 0;
    public static final int INVALID_FILE = 1;
    public static final int ACCESS_DENIED = 2;
    public static final int UNSUPPORTED_ERROR = 98;
    public static final int UNKNOWN_ERROR = 99;

    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String MANAGE = "manage";
    public static final int UNSET = 0;
    public static final int GRANTED = 1;
    public static final int DENIED = 2;
    public static final int INHERITED = 4;
    public static final int SET = 8;

    public static final String JNT_FILE = Constants.JAHIANT_FILE;
    public static final String JNT_FOLDER = Constants.JAHIANT_FOLDER;

    Node getRealNode();

    int getTransactionStatus();

    JahiaUser getUser();

    boolean isValid();

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

    Map<String, List<String>> getAvailablePermissions();

    boolean isWriteable();

    boolean hasPermission(String perm);

    Set<String> comparePermsWithField(JahiaField theField, JahiaContainer theContainer);

    void alignPermsWithField(JahiaField theField, Set<String> users);

    boolean changePermissions (String user, String perms);

    boolean changePermissions (String user, Map<String,String> perm);

    boolean revokePermissions (String user);

    boolean revokeAllPermissions ();

    boolean getAclInheritanceBreak();

    boolean setAclInheritanceBreak(boolean inheritance);

    JCRNodeWrapper createCollection (String name) throws RepositoryException;

    JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException;

    JahiaFileField getJahiaFileField ();

    String getStorageName();

    Exception getException();

    String getAbsoluteUrl(ParamBean jParams);

    String getUrl();

    String getAbsoluteWebdavUrl(ParamBean jParams);

    String getWebdavUrl();

    List<String> getThumbnails();

    String getThumbnailUrl(String name);

    List<JCRNodeWrapper> getChildren();

    boolean isVisible();

    Map<String, String> getPropertiesAsString();

    String getPrimaryNodeTypeName();

    List<String> getNodeTypes();

    boolean isCollection();

    boolean isFile();

    boolean isPortlet();

    Date getLastModifiedAsDate();

    Date getContentLastModifiedAsDate();

    Date getCreationDateAsDate();

    String getCreationUser();

    String getModificationUser();

    String getPropertyAsString(String name);

    void setProperty(String namespace, String name, String value) throws RepositoryException;

    public List<Item> getAncestors() throws RepositoryException;

    boolean renameFile(String newName) throws RepositoryException;

    boolean moveFile(String dest) throws RepositoryException;

    boolean moveFile(String dest, String name) throws RepositoryException;

    boolean copyFile(String dest) throws RepositoryException;

    boolean copyFile(String dest, String name) throws RepositoryException;

    boolean copyFile(JCRNodeWrapper node, String name) throws RepositoryException;

    int deleteFile ();

    boolean lockAsSystemAndStoreToken();

    boolean lockAndStoreToken();

    boolean forceUnlock();

    String getLockOwner();

    void versionFile();

    boolean isVersioned();

    void checkpoint();

    List<String> getVersions();

    JCRNodeWrapper getFrozenVersion(String name);

    JCRStoreProvider getJCRProvider();

    JCRStoreProvider getProvider();

    JCRFileContent getFileContent();

    List<UsageEntry> findUsages();

    List<UsageEntry> findUsages(boolean onlyLocked);

    List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked);

    String getPath();

    String getName();

    boolean isLocked();

    boolean isLockable();

    JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    JCRNodeWrapper addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException;
}
