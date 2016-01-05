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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.zip.ZipEntryCharsetDetector;
import org.slf4j.Logger;

import javax.jcr.ItemExistsException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
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
     * @param zipname         the archive name
     * @param files           the file list
     * @return null if everything went fine, a list of untreated files otherwise
     * @throws RepositoryException
     */
    public List<String> zipFiles(final JCRNodeWrapper parentDirectory, final String zipname, final List<JCRNodeWrapper> files) {
        List<String> missedPaths = null;
        File tmp = null;
        try {
            tmp = File.createTempFile("jahiazip", ".zip");
            final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            final byte[] buffer = new byte[4096];
            String parentDir = parentDirectory.getPath();
            if (!parentDir.endsWith("/")) {
                parentDir = parentDir + "/";
            }
            for (JCRNodeWrapper file : files) {
                try {
                    zipFileEntry(file, zout, buffer, parentDir);
                } catch (IOException e) {
                    logger.error("Error zipping file " + file.getPath(), e);
                    if (missedPaths == null) {
                        missedPaths = new ArrayList<>();
                    }
                    missedPaths.add(file.getPath());
                }
            }
            InputStream is = new BufferedInputStream(new FileInputStream(tmp));
            try {
                zout.close();
                JCRNodeWrapper result = parentDirectory.uploadFile(zipname, is, "application/zip");
                result.saveSession();
            } catch (IOException | RepositoryException e) {
                logger.error("Error writing resulting zipped file", e);
                missedPaths = new ArrayList<>();
                for (JCRNodeWrapper node : files) {
                    missedPaths.add(node.getName());
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
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

    private void zipFileEntry(final JCRNodeWrapper file, final ZipOutputStream zout, final byte[] buffer, String rootDir) throws IOException {
        ZipEntry anEntry;
        String relativePath = file.getPath().replace(rootDir, "");
        if (!file.isFile()) {
            anEntry = new ZipEntry(relativePath + "/");
            zout.putNextEntry(anEntry);
            try {
                for (final NodeIterator iterator = file.getNodes(); iterator.hasNext();) {
                    final JCRNodeWrapper fileNode = (JCRNodeWrapper) iterator.next();
                    zipFileEntry(fileNode, zout, buffer, rootDir);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            final InputStream is = file.getFileContent().downloadFile();
            if (is != null) {
                try {
                    anEntry = new ZipEntry(relativePath);
                    zout.putNextEntry(anEntry);
                    int bytesIn;
                    while ((bytesIn = is.read(buffer)) != -1) {
                        zout.write(buffer, 0, bytesIn);
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    public boolean unzipFile(final JCRNodeWrapper zipfile, final JCRNodeWrapper destination,
            JCRSessionWrapper currentUserSession) throws RepositoryException {
        NoCloseZipInputStream zis = null;
        try {
            JCRFileContent fileContent = zipfile.getFileContent();
            Charset charset = ZipEntryCharsetDetector.detect(fileContent);
            if (logger.isDebugEnabled()) {
                logger.debug("Unzipping content of the node {} using charset {}", zipfile.getPath(), charset);
            }
            zis = new NoCloseZipInputStream(fileContent.downloadFile(), charset);
            return doUnzipContent(zis, destination.getPath(), currentUserSession);
        } finally {
            if (zis != null) {
                try {
                    zis.reallyClose();
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.warn("Unable to close the ZIP file for node " + zipfile.getPath(), e);
                    } else {
                        logger.warn("Unable to close the ZIP file for node {}", zipfile.getPath());
                    }
                }
            }
        }
    }

    private boolean doUnzipContent(final NoCloseZipInputStream zis, final String dest, JCRSessionWrapper currentUserSession) throws RepositoryException {
        List<String> errorFiles = new ArrayList<String>();
        boolean result = false;
        try {
            ZipEntry zipentry;

            while ((zipentry = zis.getNextEntry()) != null) {
                String filename = null;
                try {
                    filename = zipentry.getName().replace('\\', '/');
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unzip file (" + zipentry.getMethod() + ")" + filename);
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

                    if (zipentry.isDirectory()) {
                        String folderName = JCRContentUtils.escapeLocalNodeName(filename);
                        if (!target.hasNode(folderName)) {
                            try {
                                target.createCollection(folderName);
                            } catch (ItemExistsException e) {
                                // ignore - it is already there
                            }
                        }
                    } else {
                        target.uploadFile(filename, zis, JCRContentUtils.getMimeType(filename));
                    }
                    result = true;
                } catch (InternalError err) {
                    logger.error("Error when unzipping file", err);
                    if (filename != null) {
                        errorFiles.add(filename);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error when unzipping file", e);
            result = false;
        } catch (InternalError err) {
            logger.error("Error when unzipping file, " + err.getMessage(), err);
            result = false;
        }

        return result;
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
                    StringBuilder errors = new StringBuilder(Messages.getInternal("label.gwt.error.the.following.files.could.not.be.zipped",uiLocale));
                    for (String err : errorPaths) {
                        errors.append("\n").append(err);
                    }
                    throw new GWTJahiaServiceException(errors.toString());
                }
            } else {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.directory.is.not.writable",uiLocale, parent.getPath()));
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(Messages.getInternal("label.gwt.error.the.following.files.could.not.be.zipped",uiLocale));
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
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.directory.is.not.writable",uiLocale, parent.getPath()));
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
