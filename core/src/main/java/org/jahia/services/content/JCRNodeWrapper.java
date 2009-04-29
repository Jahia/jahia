/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.api.Constants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;

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

    Map<String, List<String[]>> getAclEntries();

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

    String getPropertyAsString(String namespace, String name);

    void setProperty(String namespace, String name, String value) throws RepositoryException;

    boolean renameFile(String newName);

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

    JCRNodeWrapper getVersion(String name);

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
