/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.content.files.FileCacheManager;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Versioning helper class for GWT services.
 * User: toto
 * Date: Feb 2, 2009
 * Time: 7:03:31 PM
 */
public class VersioningHelper implements InitializingBean {
    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy_MM_dd_HH_mm_ss");

    private static Logger logger = LoggerFactory.getLogger(VersioningHelper.class);

    /**
     * Returns the formatted timestamp for the version label.
     * @param timestamp the time stamp for the label
     * @return the formatted timestamp for the version label
     */
    public static String formatForLabel(long timestamp) {
        return DF.format(timestamp);
    }
    
    /**
     * Returns the label for the new version of the file with the timestamp.
     * @param timestamp the time stamp for the label
     * @return the label for the new version of the file with the timestamp
     */
    public static String getVersionLabel(long timestamp) {
        return "uploaded_at_" + formatForLabel(timestamp);
    }
    
    /**
     * Returns the label for the restored version of the file with the timestamp.
     * @param timestamp the time stamp for the label
     * @return the label for the restored version of the file with the timestamp
     */
    public static String getRestoreVersionLabel(long timestamp) {
        return "restored_at_" + formatForLabel(timestamp);
    }
    
    private CacheService cacheService;
    private JCRVersionService versionService;
    private FileCacheManager cacheManager;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setVersionService(JCRVersionService versionService) {
        this.versionService = versionService;
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
            } catch (Throwable e) {
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
     *
     */
    public void addNewVersionFile(JCRNodeWrapper node, String tmpName) throws GWTJahiaServiceException {
        try {
            if (node != null) {
                JCRSessionWrapper session = node.getSession();
                VersionManager versionManager = session.getWorkspace().getVersionManager();
                if (!node.isVersioned()) {
                    node.versionFile();
                    session.save();
                }
                VersionIterator allVersions = versionManager.getVersionHistory(node.getPath()).getAllVersions();
                if(allVersions.getSize()==1) {
                    // First version ever apart root version
                    versionManager.checkpoint(node.getPath());
                    versionService.addVersionLabel(node, getVersionLabel(node.getProperty("jcr:created").getDate().getTime().getTime()));
                }
                versionManager.checkout(node.getPath());
                GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(tmpName);
                InputStream is = null;
                try {
                    is = item.getStream();
                    node.getFileContent().uploadFile(is, item.getContentType());
                } catch (FileNotFoundException e) {
                    throw new GWTJahiaServiceException(e.getMessage());
                } finally {
                    IOUtils.closeQuietly(is);
                    item.dispose();
                }
                
                session.save();
                versionManager.checkpoint(node.getPath());
                versionService.addVersionLabel(node, getVersionLabel(System.currentTimeMillis()));
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

    public void afterPropertiesSet() throws Exception {
        cacheManager = FileCacheManager.getInstance();
    }
}
