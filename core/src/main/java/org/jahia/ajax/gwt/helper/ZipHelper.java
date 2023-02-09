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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.zip.ZipEntryCharsetDetector;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRFileContent;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Helper class for ZIP file manipulations.
 * User: rfelden
 * Date: 19 sept. 2008
 * Time: 09:33:04
 */
public class ZipHelper {

    private static volatile ZipHelper instance;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ZipHelper.class);

    public static ZipHelper getInstance() {
        if (instance == null) {
            synchronized (ZipHelper.class) {
                if (instance == null) {
                    instance = new ZipHelper();
                }
            }
        }
        return instance;
    }

    /**
     * @param parentDirectory the directory to create the archive into
     * @param zipName         the archive name
     * @param files           the file list
     * @return null if everything went fine, a list of untreated files otherwise
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<String> zipFiles(final JCRNodeWrapper parentDirectory, final String zipName, final List<JCRNodeWrapper> files) {
        List<String> missedPaths;
        File tmp = null;
        try {
            tmp = File.createTempFile("jahiazip", ".zip");
            missedPaths = zipRequestedFilesInTmpArchiveFile(parentDirectory, files, tmp);
            // zipOutputStream is closed by previous try-with-resources
            // Copy tmp file into zipName
            missedPaths = uploadTmpArchiveFileToDestinationDirectory(parentDirectory, zipName, files, missedPaths, tmp);
        } catch (final IOException e) {
            logger.error("Error creating zipped file", e);
            missedPaths = new ArrayList<>();
            for (JCRNodeWrapper node : files) {
                missedPaths.add(node.getName());
            }
        } finally {
            FileUtils.deleteQuietly(tmp);
        }
        return missedPaths;
    }

    private List<String> uploadTmpArchiveFileToDestinationDirectory(JCRNodeWrapper parentDirectory, String zipName, List<JCRNodeWrapper> files, List<String> missedPaths, File tmp) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(tmp))) {
            JCRNodeWrapper result = parentDirectory.uploadFile(zipName, is, "application/zip");
            result.saveSession();
        } catch (IOException | RepositoryException e) {
            logger.error("Error writing resulting zipped file", e);
            missedPaths = new ArrayList<>();
            for (JCRNodeWrapper node : files) {
                missedPaths.add(node.getName());
            }
        }
        return missedPaths;
    }

    private List<String> zipRequestedFilesInTmpArchiveFile(JCRNodeWrapper parentDirectory, List<JCRNodeWrapper> files, File tmp) throws IOException {
        List<String> missedPaths = null;
        // Zip files in tmp file
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))) {
            final byte[] buffer = new byte[4096];
            String parentDir = parentDirectory.getPath();
            if (!parentDir.endsWith("/")) {
                parentDir = parentDir + "/";
            }
            for (JCRNodeWrapper file : files) {
                try {
                    zipFileEntry(file, zipOutputStream, buffer, parentDir);
                } catch (IOException e) {
                    logger.error("Error zipping file " + file.getPath(), e);
                    if (missedPaths == null) {
                        missedPaths = new ArrayList<>();
                    }
                    missedPaths.add(file.getPath());
                }
            }
        }
        return missedPaths;
    }

    private void zipFileEntry(final JCRNodeWrapper file, final ZipOutputStream zout, final byte[] buffer, String rootDir) throws IOException {
        ZipEntry anEntry;
        String relativePath = file.getPath().replace(rootDir, "");
        if (!file.isFile()) {
            anEntry = new ZipEntry(relativePath + "/");
            zout.putNextEntry(anEntry);
            try {
                for (final NodeIterator iterator = file.getNodes(); iterator.hasNext(); ) {
                    final JCRNodeWrapper fileNode = (JCRNodeWrapper) iterator.next();
                    zipFileEntry(fileNode, zout, buffer, rootDir);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            try (InputStream is = file.getFileContent().downloadFile()) {
                if(is != null) {
                    anEntry = new ZipEntry(relativePath);
                    zout.putNextEntry(anEntry);
                    int bytesIn;
                    while ((bytesIn = is.read(buffer)) != -1) {
                        zout.write(buffer, 0, bytesIn);
                    }
                }
            }
        }
    }

    @SuppressWarnings("java:S2093")
    public boolean unzipFile(final JCRNodeWrapper zipfile, final JCRNodeWrapper destination,
            JCRSessionWrapper currentUserSession) throws RepositoryException {
        InputStream is = null;
        File f = null;
        ZipFile zip = null;

        try {
            JCRFileContent fileContent = zipfile.getFileContent();
            Charset charset = ZipEntryCharsetDetector.detect(fileContent);
            if (logger.isDebugEnabled()) {
                logger.debug("Unzipping content of the node {} using charset {}", zipfile.getPath(), charset);
            }

            is = fileContent.downloadFile();
            f = File.createTempFile(UUID.randomUUID() + ".zip", "");
            FileUtils.copyInputStreamToFile(is, f);
            zip = new ZipFile(f, charset);
            return doUnzipContent(zip, destination.getPath(), currentUserSession);
        } catch (IOException e) {
            logger.error("Failed to read file", e);
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.warn("Unable to close the ZIP file for node " + zipfile.getPath(), e);
                    } else {
                        logger.warn("Unable to close the ZIP file for node {}", zipfile.getPath());
                    }
                }
            }
            FileUtils.deleteQuietly(f);
            try {
                if (zip != null) {
                    // Closes streams opened in uzip routine as well
                    zip.close();
                }
            } catch (IOException e) {
                logger.error("Failed to close stream", e);
            }
        }
    }

    private boolean doUnzipContent(final ZipFile zip, final String dest, JCRSessionWrapper currentUserSession) {
        List<String> errorFiles = new ArrayList<>();
        zip.stream().forEach(zipEntry -> {
            String filename = null;
            try {
                filename = zipEntry.getName().replace('\\', '/');
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Unzip file (%d)%s", zipEntry.getMethod(), filename));
                }
                if (filename.endsWith("/")) {
                    filename = filename.substring(0, filename.length() - 1);
                }
                int endIndex = filename.lastIndexOf('/');
                String parentName = dest;
                if (endIndex > -1) {
                    parentName += "/" + filename.substring(0, endIndex);
                    filename = filename.substring(endIndex + 1);
                }
                JCRNodeWrapper target = ensureDir(parentName, currentUserSession);

                if (zipEntry.isDirectory()) {
                    String folderName = JCRContentUtils.escapeLocalNodeName(filename);
                    if (!target.hasNode(folderName)) {
                        target.createCollection(folderName);
                    }
                } else {
                    target.uploadFile(filename, zip.getInputStream(zipEntry), JCRContentUtils.getMimeType(filename));
                }
            } catch (InternalError | RepositoryException | IOException err) {
                logger.error("Error when unzipping file", err);
                errorFiles.add(filename);
            }
        });

        return errorFiles.isEmpty();
    }

    private JCRNodeWrapper ensureDir(String path, JCRSessionWrapper currentUserSession) throws RepositoryException {
        try {
            return currentUserSession.getNode(JCRContentUtils.escapeNodePath(path));
        } catch (RepositoryException e) {
            if (e instanceof PathNotFoundException || e.getCause() != null && e.getCause() instanceof MalformedPathException) {
                int endIndex = path.lastIndexOf('/');
                if (endIndex == -1) {
                    return null;
                }
                JCRNodeWrapper parentDir = ensureDir(path.substring(0, endIndex), currentUserSession);
                if (parentDir == null) {
                    return null;
                }
                return parentDir.createCollection(JCRContentUtils.escapeLocalNodeName(path.substring(path.lastIndexOf('/') + 1)));
            } else {
                throw e;
            }
        }
    }

    public void zip(List<String> paths, String archiveName, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        if (!archiveName.endsWith(".zip") && !archiveName.endsWith(".ZIP")) {
            archiveName = new StringBuilder(archiveName).append(".zip").toString();
        }
        List<String> missedPaths = new ArrayList<String>();
        List<JCRNodeWrapper> nodesToZip = new ArrayList<JCRNodeWrapper>();
        for (String path : paths) {
            JCRNodeWrapper nodeToZip;
            try {
                nodeToZip = currentUserSession.getNode(path);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
                continue;
            }
            nodesToZip.add(nodeToZip);
        }
        if (nodesToZip.size() > 0) {
            String firstPath = nodesToZip.get(0).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > 0) {
                parentPath = firstPath.substring(0, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent;
            try {
                parent = currentUserSession.getNode(parentPath);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
            }
            if (parent.hasPermission("jcr:addChildNodes") && !parent.isLocked()) {
                List<String> errorPaths = zipFiles(parent, archiveName, nodesToZip);
                if (errorPaths != null) {
                    errorPaths.addAll(missedPaths);
                    StringBuilder errors = new StringBuilder(Messages.getInternal("label.gwt.error.the.following.files.could.not.be.zipped", uiLocale));
                    for (String err : errorPaths) {
                        errors.append("\n").append(err);
                    }
                    throw new GWTJahiaServiceException(errors.toString());
                }
            } else {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.directory.is.not.writable", uiLocale, parent.getPath()));
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(Messages.getInternal("label.gwt.error.the.following.files.could.not.be.zipped", uiLocale));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public void unzip(List<String> paths, boolean removeArchive, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        List<JCRNodeWrapper> nodesToUnzip = new ArrayList<JCRNodeWrapper>();
        for (String path : paths) {
            JCRNodeWrapper nodeToUnzip;
            try {
                nodeToUnzip = currentUserSession.getNode(JCRContentUtils.escapeNodePath(path));
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
                continue;
            }
            nodesToUnzip.add(nodeToUnzip);
        }
        if (nodesToUnzip.size() > 0) {
            String firstPath = nodesToUnzip.get(0).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > 0) {
                parentPath = firstPath.substring(0, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent;
            try {
                parent = currentUserSession.getNode(parentPath);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
            }
            if (parent.hasPermission("jcr:addChildNodes") && !parent.isLocked()) {
                for (JCRNodeWrapper nodeToUnzip : nodesToUnzip) {
                    try {
                        if (!unzipFile(nodeToUnzip, parent, currentUserSession)) {
                            missedPaths.add(nodeToUnzip.getName());
                        } else if (removeArchive) {
                            try {
                                nodeToUnzip.remove();
                            } catch (RepositoryException e) {
                                logger.error("Issue when trying to delete original archive " + nodeToUnzip.getPath(), e);
                            }
                        }
                    } catch (RepositoryException e) {
                        missedPaths.add(nodeToUnzip.getName());
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.directory.is.not.writable", uiLocale, parent.getPath()));
            }
            try {
                parent.saveSession();
            } catch (RepositoryException e) {
                logger.error("Could not save changes in " + parent.getPath(), e);
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be unzipped:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }
}
