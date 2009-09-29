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

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRVersionHistory;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 2, 2009
 * Time: 7:03:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersioningHelper {
    private static JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
    private static Logger logger = Logger.getLogger(VersioningHelper.class);

    /**
     * Activate versionning
     *
     * @param pathes
     * @param jParams
     */
    public static void activateVersioning(List<String> pathes, ProcessingContext jParams) {
        for (String path : pathes) {
            try {
                JCRSessionWrapper s = sessionFactory.getCurrentUserSession();
                JCRNodeWrapper node = s.getNode(path);
                if (!node.isVersioned()) {
                    node.versionFile();
                    s.save();
                }
            } catch (Throwable e) {
                logger.error(e, e);
            }
        }
    }

    /**
     * Get list of version as gwt bean list
     *
     * @param node
     * @param jParams
     * @return
     */
    public static List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, ProcessingContext jParams) {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        try {
            if (node.isVersioned()) {
                VersionHistory vh = node.getVersionHistory();
                VersionIterator vi = vh.getAllVersions();
                while (vi.hasNext()) {
                    Version v = vi.nextVersion();
                    if (!v.getName().equals("jcr:rootVersion")) {
                        JCRNodeWrapper orig = ((JCRVersionHistory) v.getContainingHistory()).getNode();
                        GWTJahiaNode n = NavigationHelper.getGWTJahiaNode(orig, false);
                        n.setUrl(orig.getUrl() + "?v=" + v.getName());
                        GWTJahiaNodeVersion jahiaNodeVersion = new GWTJahiaNodeVersion(v.getUUID(), v.getName(), v.getCreated().getTime());
                        jahiaNodeVersion.setNode(n);
                        versions.add(jahiaNodeVersion);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
        return versions;
    }


    /**
     * Activate versioning if nested and add a new version
     *
     * @param node
     * @param tmpName
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public static void addNewVersionFile(JCRNodeWrapper node, String tmpName) throws GWTJahiaServiceException {
        try {
            if (node != null) {
                if (!node.isVersioned()) {
                    node.versionFile();
                    node.save();
                }
                node.checkout();
                node.getFileContent().uploadFile(GWTFileManagerUploadServlet.getItem(tmpName).file, GWTFileManagerUploadServlet.getItem(tmpName).contentType);
                node.save();
                node.checkpoint();

                logger.debug("Number of version: " + node.getVersions().size());

            } else {
                logger.error("Could not add version to a null file.");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Restore node version
     *
     * @param nodeUuid
     * @param versionUuid
     * @param ctx
     */
    public static void restoreNode(String nodeUuid, String versionUuid, ProcessingContext ctx) {
        try {
            JCRNodeWrapper node = (JCRNodeWrapper) sessionFactory.getCurrentUserSession().getNodeByUUID(nodeUuid);
            Version version = (Version) sessionFactory.getCurrentUserSession().getNodeByUUID(versionUuid);
            node.checkout();
            node.restore(version, true);
            node.checkpoint();

            // fluch caches: To do: flush only the nested cache
            ServicesRegistry.getInstance().getCacheService().flushAllCaches();


        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
