/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.utils.JcrUtils;

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
                    JCRNodeWrapper pendingFiles = JcrUtils.getNodeCreateIfNeeded(jcrSession.getRootNode(), jcrFolderName, Constants.JAHIANT_TEMP_FOLDER);
                    JCRNodeWrapper sessionPendingFiles = JcrUtils.getNodeCreateIfNeeded(pendingFiles, finalSessionID, Constants.JAHIANT_TEMP_FOLDER);
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
