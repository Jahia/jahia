/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.content.server;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.api.Constants;
import org.jahia.bin.SessionNamedDataStorageSupport;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.*;

/**
 * File storage that keeps files in JCR.
 */
public class UploadedPendingFileStorageJcr extends SessionNamedDataStorageSupport<UploadedPendingFile> {

    private String jcrFolderName;

    public void setJcrFolderName(String jcrFolderName) {
        this.jcrFolderName = jcrFolderName;
    }

    @Override
    public void put(String sessionID, String name, final UploadedPendingFile file) {

        final String finalSessionID = Text.escapeIllegalJcrChars(sessionID);
        final String finalName = Text.escapeIllegalJcrChars(name);

        try {

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {

                @Override
                public Void doInJCR(JCRSessionWrapper jcrSession) throws RepositoryException {
                    JCRNodeWrapper pendingFiles = getFolderCreateIfNeeded(jcrSession.getRootNode(), jcrFolderName);
                    JCRNodeWrapper sessionPendingFiles = getFolderCreateIfNeeded(pendingFiles, finalSessionID);
                    if (sessionPendingFiles.hasNode(finalName)) {
                        sessionPendingFiles.getNode(finalName).remove();
                    }
                    JCRNodeWrapper fileNode = sessionPendingFiles.addNode(finalName, Constants.JAHIANT_TEMP_FILE);
                    fileNode.setProperty(Constants.JCR_MIMETYPE, file.getContentType());
                    Binary contentBinary;
                    try {
                        contentBinary = new BinaryImpl(file.getContentStream());
                    } catch (IOException e) {
                        throw new JahiaRuntimeException(e);
                    }
                    try {
                        fileNode.setProperty(Constants.JCR_DATA, contentBinary);
                    } finally {
                        contentBinary.dispose();
                    }
                    jcrSession.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public UploadedPendingFile get(String sessionID, String name) {
        try {
            return retrieve(sessionID, name);
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public UploadedPendingFile getRequired(String sessionID, String name) {
        try {
            return retrieve(sessionID, name);
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public void remove(String sessionID, String name) {

        final String finalSessionID = Text.escapeIllegalJcrChars(sessionID);
        final String finalName = Text.escapeIllegalJcrChars(name);

        try {

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {

                @Override
                public Void doInJCR(JCRSessionWrapper jcrSession) throws RepositoryException {
                    jcrSession.removeItem(getPathString(jcrFolderName, finalSessionID, finalName));
                    jcrSession.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public void removeIfExists(String sessionID) {

        final String finalSessionID = Text.escapeIllegalJcrChars(sessionID);

        try {

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {

                @Override
                public Void doInJCR(JCRSessionWrapper jcrSession) throws RepositoryException {
                    jcrSession.removeItem(getPathString(jcrFolderName, finalSessionID));
                    jcrSession.save();
                    return null;
                }
            });
        } catch (PathNotFoundException e) {
            // Session folder does not exist.
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private JCRNodeWrapper getFolderCreateIfNeeded(JCRNodeWrapper parent, String name) throws RepositoryException {

        if (parent.hasNode(name)) {
            return parent.getNode(name);
        }
        parent.addNode(name, Constants.JAHIANT_TEMP_FOLDER);
        parent.getSession().save();

        // Even though we check target folder existence before adding a new one, there might be a concurrent thread doing the same
        // simultaneously, so we both may succeed adding a new folder in case multiple equally named child nodes are allowed by the
        // parent node (this is the actual case with the root node). So, to make sure any threads always use the same folder, just
        // pick the one with index equal to 1 (this is what getNode() does in case there are multiple equally named items exist).
        return parent.getNode(name);
    }

    private static String getPathString(String... pathElements) {
        return '/' + StringUtils.join(pathElements, '/');
    }

    private UploadedPendingFile retrieve(String sessionID, String name) throws RepositoryException {

        final String finalSessionID = Text.escapeIllegalJcrChars(sessionID);
        final String finalName = Text.escapeIllegalJcrChars(name);

        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<UploadedPendingFile>() {

            @Override
            public UploadedPendingFile doInJCR(JCRSessionWrapper jcrSession) throws RepositoryException {

                JCRNodeWrapper fileNode = jcrSession.getNode(getPathString(jcrFolderName, finalSessionID, finalName));
                final Binary contentBinary = fileNode.getProperty(Constants.JCR_DATA).getBinary();
                final String contentType = fileNode.getPropertyAsString(Constants.JCR_MIMETYPE);

                return new UploadedPendingFile() {

                    @Override
                    public String getContentType() {
                        return contentType;
                    }

                    @Override
                    public InputStream getContentStream() {
                        try {
                            return contentBinary.getStream();
                        } catch (RepositoryException e) {
                            throw new JahiaRuntimeException(e);
                        }
                    }

                    @Override
                    public void close() {
                        contentBinary.dispose();
                    }
                };
            }
        });
    }
}
