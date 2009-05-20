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
package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;

import javax.jcr.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSRepositoryImpl implements Repository {
    
    private static final transient Logger logger = Logger.getLogger(VFSRepositoryImpl.class);
    
    private String root;
    private String rootPath;

    private FileSystemManager manager;

    public VFSRepositoryImpl(String root) {
        this.root = root;

        try {
            manager = VFS.getManager();
            rootPath = getFile("/").getName().getPath();
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getRoot() {
        return root;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String[] getDescriptorKeys() {
        return new String[0];
    }

    public String getDescriptor(String s) {
        return null;
    }

    public Session login(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public Session login() throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public FileObject getFile(String path) throws FileSystemException {
        return manager.resolveFile(root+path);
    }

}
