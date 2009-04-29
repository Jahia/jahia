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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.webdav.AbstractLocatorFactory;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 févr. 2008
 * Time: 16:47:37
 * To change this template use File | Settings | File Templates.
 */
public class DavLocatorFactoryImpl extends AbstractLocatorFactory  {


    /**
     * Create a new factory
     *
     * @param pathPrefix Prefix, that needs to be removed in order to retrieve
     *                   the path of the repository item from a given <code>DavResourceLocator</code>.
     */
    public DavLocatorFactoryImpl(String pathPrefix) {
        super(pathPrefix);
    }

    /**
     *
     * @see org.apache.jackrabbit.webdav.AbstractLocatorFactory#getRepositoryPath(String, String)
     */
    protected String getRepositoryPath(String resourcePath, String wspPath) {
        if (resourcePath == null) {
            return resourcePath;
        }

        if (resourcePath.equals(wspPath) || startsWithWorkspace(resourcePath, wspPath)) {
            String repositoryPath = resourcePath.substring(wspPath.length());
            repositoryPath = repositoryPath.replace("[","\\5B");
            repositoryPath = repositoryPath.replace("]","\\5C");
            repositoryPath = repositoryPath.replace("'","\\27");

            return (repositoryPath.length() == 0) ? "/" : repositoryPath;
        } else {
            throw new IllegalArgumentException("Unexpected format of resource path.");
        }
    }

    /**
     *
     * @see org.apache.jackrabbit.webdav.AbstractLocatorFactory#getResourcePath(String, String)
     */
    protected String getResourcePath(String repositoryPath, String wspPath) {
        if (repositoryPath == null) {
            throw new IllegalArgumentException("Cannot build resource path from 'null' repository path");
        }
        repositoryPath = repositoryPath.replace("\\5B","[");
        repositoryPath = repositoryPath.replace("\\5C","]");
        repositoryPath = repositoryPath.replace("\\27","'");
        return (startsWithWorkspace(repositoryPath, wspPath)) ? repositoryPath : wspPath + repositoryPath;
    }

    private boolean startsWithWorkspace(String repositoryPath, String wspPath) {
        if (wspPath == null) {
            return true;
        } else {
            return repositoryPath.startsWith(wspPath + "/");
        }
    }
}
