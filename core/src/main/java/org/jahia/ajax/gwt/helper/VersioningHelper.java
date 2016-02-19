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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.UploadedPendingFile;
import org.jahia.bin.SessionNamedDataStorage;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.content.files.FileCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Versioning helper class for GWT services.
 * User: toto
 * Date: Feb 2, 2009
 * Time: 7:03:31 PM
 */
public class VersioningHelper {

    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy_MM_dd_HH_mm_ss");
    private static final Logger logger = LoggerFactory.getLogger(VersioningHelper.class);

    /**
     * Returns the formatted timestamp for the version label.
     *
     * @param timestamp the time stamp for the label
     * @return the formatted timestamp for the version label
     */
    public static String formatForLabel(long timestamp) {
        return DF.format(timestamp);
    }

    /**
     * Returns the label for the new version of the file with the timestamp.
     *
     * @param timestamp the time stamp for the label
     * @return the label for the new version of the file with the timestamp
     */
    public static String getVersionLabel(long timestamp) {
        return "uploaded_at_" + formatForLabel(timestamp);
    }

    /**
     * Returns the label for the new version of the file with the current timestamp.
     *
     * @param timestamp the time stamp for the label
     * @return the label for the new version of the file with the current timestamp
     */
    public static String getVersionLabelCurrent() {
        return getVersionLabel(System.currentTimeMillis());
    }

    /**
     * Returns the label for the restored version of the file with the timestamp.
     *
     * @param timestamp the time stamp for the label
     * @return the label for the restored version of the file with the timestamp
     */
    public static String getRestoreVersionLabel(long timestamp) {
        return "restored_at_" + formatForLabel(timestamp);
    }

    private CacheService cacheService;
    private JCRVersionService versionService;
    private FileCacheManager cacheManager;
    private SessionNamedDataStorage<UploadedPendingFile> fileStorage;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setVersionService(JCRVersionService versionService) {
        this.versionService = versionService;
    }

    public void setCacheManager(FileCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setFileStorage(SessionNamedDataStorage<UploadedPendingFile> fileStorage) {
        this.fileStorage = fileStorage;
    }

    /**
     * Activate versionning
     *
     * @param pathes
     * @param currentUserSession
     */
    public void activateVersioning(List<String> pathes, JCRSessionWrapper currentUserSession) {
        for (String path : pathes) {
            try {
                JCRNodeWrapper node = currentUserSession.getNode(path);
                if (!node.isVersioned()) {
                    node.versionFile();
                    currentUserSession.save();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Activate versioning if nested and add a new version
     *
     * @param node
     * @param tmpName
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     */
    public void addNewVersionFile(JCRNodeWrapper node, String tmpName, String httpSessionID) throws GWTJahiaServiceException {
        try {
            if (node != null) {

                JCRSessionWrapper session = node.getSession();
                VersionManager versionManager = session.getWorkspace().getVersionManager();
                if (!node.isVersioned()) {
                    node.versionFile();
                    session.save();
                }
                VersionIterator allVersions = versionManager.getVersionHistory(node.getPath()).getAllVersions();
                if (allVersions.getSize() == 1) {
                    // First version ever apart root version
                    versionManager.checkpoint(node.getPath());
                    versionService.addVersionLabel(node, getVersionLabel(node.getProperty("jcr:created").getDate().getTime().getTime()));
                }
                versionManager.checkout(node.getPath());
                UploadedPendingFile item = fileStorage.getRequired(httpSessionID, tmpName);
                InputStream is = null;
                try {
                    is = item.getContentStream();
                    node.getFileContent().uploadFile(is, JCRContentUtils.getMimeType(StringUtils.isNotEmpty(tmpName) ? tmpName : node.getName(), item.getContentType()));
                } finally {
                    IOUtils.closeQuietly(is);
                    fileStorage.remove(httpSessionID, tmpName);
                }

                session.save();
                versionManager.checkpoint(node.getPath());
                versionService.addVersionLabel(node, getVersionLabelCurrent());
                cacheManager.invalidate(session.getWorkspace().getName(), node.getPath());
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of version: " + node.getVersions().size());
                }

            } else {
                logger.error("Could not add version to a null file.");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void restoreVersionLabel(String nodeUuid, Date versionDate, String versionLabel, boolean allSubTree,
                                    JCRSessionWrapper currentUserSession) {
        try {
            JCRNodeWrapper node = currentUserSession.getNodeByUUID(nodeUuid);
            versionService.restoreVersionLabel(node, versionDate, versionLabel, allSubTree);
            currentUserSession.save();
            versionService.addVersionLabel(node, getRestoreVersionLabel(System.currentTimeMillis()));
            // flush caches: Todo: flush only the nested cache
            cacheService.flushAllCaches();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
