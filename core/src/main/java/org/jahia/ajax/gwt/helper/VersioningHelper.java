/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import javax.jcr.Repository;
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

                boolean supportVersioning = node.getProvider().getRepository().getDescriptorValue(Repository.OPTION_VERSIONING_SUPPORTED).getBoolean();
                JCRSessionWrapper session = node.getSession();

                if(supportVersioning) {
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
                }

                UploadedPendingFile item = fileStorage.getRequired(httpSessionID, tmpName);
                try {
                    InputStream is = null;
                    try {
                        is = item.getContentStream();
                        node.getFileContent().uploadFile(is, JCRContentUtils.getMimeType(StringUtils.isNotEmpty(tmpName) ? tmpName : node.getName(), item.getContentType()));
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                } finally {
                    item.close();
                    fileStorage.remove(httpSessionID, tmpName);
                }
                session.save();

                if(supportVersioning && JCRContentUtils.needVersion(node, versionService.getVersionedTypes())) {
                    session.getWorkspace().getVersionManager().checkpoint(node.getPath());
                    versionService.addVersionLabel(node, getVersionLabelCurrent());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Number of version: " + node.getVersions().size());
                    }
                }

                cacheManager.invalidate(session.getWorkspace().getName(), node.getPath());
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
