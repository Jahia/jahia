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
package org.jahia.ajax.gwt.content.server.helper;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.zip.ZipOutputStream;
import org.jahia.utils.zip.ZipEntry;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 19 sept. 2008
 * Time: 09:33:04
 * To change this template use File | Settings | File Templates.
 */
public class JCRZipTools {

    private static Logger logger = Logger.getLogger(JCRZipTools.class) ;

    /**
     *
     * @param parentDirectory the directory to create the archive into
     * @param zipname the archive name
     * @param files the file list
     * @return null if everything went fine, a list of untreated files otherwise
     * @throws RepositoryException
     */
    public static List<String> zipFiles(final JCRNodeWrapper parentDirectory, final String zipname, final List<JCRNodeWrapper> files) {
        List<String> missedPaths = null ;
        try {
            final File tmp = File.createTempFile("jahiazip", null);
            final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmp));
            final byte[] buffer = new byte[4096];
            String parentDir = parentDirectory.getPath() ;
            if (!parentDir.endsWith("/")) {
                parentDir = new StringBuilder(parentDir).append("/").toString() ;
            }
            for (JCRNodeWrapper file: files) {
                try {
                    zipFileEntry(file, zout, buffer, parentDir);
                } catch (IOException e) {
                    logger.error("Error zipping file " + file.getPath(), e) ;
                    if (missedPaths == null) {
                        missedPaths = new ArrayList<String>() ;
                    }
                    missedPaths.add(file.getPath()) ;
                }
            }
            try {
                zout.close();
                JCRNodeWrapper result = parentDirectory.uploadFile(zipname, new FileInputStream(tmp), "application/zip");
                result.saveSession();
            } catch (IOException e) {
                logger.error("Error writing resulting zipped file", e);
                missedPaths = new ArrayList<String>() ;
                for (JCRNodeWrapper node: files) {
                    missedPaths.add(node.getName()) ;
                }
            } catch (RepositoryException e) {
                logger.error("Error writing resulting zipped file", e);
                missedPaths = new ArrayList<String>() ;
                for (JCRNodeWrapper node: files) {
                    missedPaths.add(node.getName()) ;
                }
            }
            tmp.delete();
        } catch (final IOException e) {
            logger.error("Error creating zipped file", e);
            missedPaths = new ArrayList<String>() ;
            for (JCRNodeWrapper node: files) {
                missedPaths.add(node.getName()) ;
            }
        }
        return missedPaths;
    }

    private static void zipFileEntry(final JCRNodeWrapper file, final ZipOutputStream zout, final byte[] buffer, String rootDir) throws IOException {
        ZipEntry anEntry;
        String relativePath = file.getPath().replace(rootDir, "") ;
        if (file.isCollection()) {
            anEntry = new org.jahia.utils.zip.ZipEntry(relativePath + "/");
            zout.putNextEntry(anEntry);
            List l = file.getChildren();
            for (final Iterator iterator = l.iterator(); iterator.hasNext();) {
                final JCRNodeWrapper fileNode = (JCRNodeWrapper) iterator.next();
                zipFileEntry(fileNode, zout, buffer, rootDir);
            }
        } else {
            final InputStream is = file.getFileContent().downloadFile();
            if (is != null) {
                try {
                    anEntry = new org.jahia.utils.zip.ZipEntry(relativePath);
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

    public static boolean unzipFile(final JCRNodeWrapper zipfile, final JCRNodeWrapper destination, final JahiaUser user) throws RepositoryException {
        InputStream in = zipfile.getFileContent().downloadFile();
        return doUnzipContent(in, destination.getPath(), user);
    }

    private static boolean doUnzipContent(final InputStream in, final String dest, final JahiaUser user)  throws RepositoryException{
        List<String> errorFiles = new ArrayList<String>() ;
        boolean result = false;
        NoCloseZipInputStream zis = null;
        try {
            zis = new NoCloseZipInputStream(in);
            ZipEntry zipentry;

            while ((zipentry = zis.getNextEntry()) != null) {
                String filename = null;
                try {
                    filename = zipentry.getName();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unzip file (" + zipentry.getMethod() + ")" + filename);
                    }
                    if (filename.endsWith("/")) {
                        filename = filename.substring(0,filename.length()-1);
                    }
                    int endIndex = filename.lastIndexOf('/');
                    String parentName = dest;
                    if (endIndex > -1) {
                        parentName += "/" + filename.substring(0, endIndex);
                        filename = filename.substring(endIndex + 1);
                    }
                    JCRNodeWrapper target = ensureDir(parentName, user);

                    if (zipentry.isDirectory()) {
                        target.createCollection(filename);
                    } else {
                        String contentType = Jahia.getStaticServletConfig().getServletContext().getMimeType(filename);
                        target.uploadFile(filename, zis, contentType);
                    }
                    result = true;
                } /*catch (IOException e) {
                    logger.error("Error when unzipping file", e);
                    if (filename != null) {
                        errorFiles.add(filename);
                    }
                } */catch (InternalError err) {
                    logger.error("Error when unzipping file", err);
                    if (filename != null) {
                        errorFiles.add(filename);
                    }
                }
            }
            zis.reallyClose();
        } catch (IOException e) {
            logger.error("Error when unzipping file", e);
            result = false;
        } catch (InternalError err) {
            logger.error("Error when unzipping file, " + err.getMessage());
            result = false;
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (Exception e) {
                    logger.error("Error when closing zip stream", e);
                }
            }
        }
        return result;
    }

    private static JCRNodeWrapper ensureDir(String path, JahiaUser user)  throws RepositoryException{
        JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService() ;
        JCRNodeWrapper dir = jcr.getFileNode(path, user);
        if (!dir.isValid()) {
            int endIndex = path.lastIndexOf('/');
            if (endIndex == -1) {
                return null;
            }
            JCRNodeWrapper parentDir = ensureDir(path.substring(0, endIndex), user);
            if (parentDir == null) {
                return null;
            }
            parentDir.createCollection(path.substring(path.lastIndexOf('/') + 1));
            dir = jcr.getFileNode(path, user);
        }
        return dir;
    }

}
