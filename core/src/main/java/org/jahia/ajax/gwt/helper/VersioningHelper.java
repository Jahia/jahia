/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.helper;

import org.slf4j.Logger;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRVersionService;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 2, 2009
 * Time: 7:03:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersioningHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VersioningHelper.class);

    private CacheService cacheService;
    private JCRVersionService versionService;

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
                    // Frist version ever apart root version
                    versionManager.checkin(node.getPath());
                    String label = "uploaded_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(node.getProperty("jcr:created").getDate().getTime());
                    versionService.addVersionLabel(node,label);
                }
                versionManager.checkout(node.getPath());
                GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(tmpName);
                FileInputStream is = null;
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
                versionManager.checkin(node.getPath());
                String label = "uploaded_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
                versionService.addVersionLabel(node,label);
                cacheService.getCache("WebdavCache").flush(true);
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
            String label = "restored_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
            versionService.addVersionLabel(node,label);
            // fluch caches: Todo: flush only the nested cache
            cacheService.flushAllCaches();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
