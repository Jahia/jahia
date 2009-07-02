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
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRVersionHistory;

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
public class JCRVersioningHelper {
    private static JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
    private static Logger logger = Logger.getLogger(JCRVersioningHelper.class);

    public static void activateVersioning(List<String> pathes, ProcessingContext jParams) {
        for (String path : pathes) {
            try {
                JCRSessionWrapper s = jcr.getThreadSession(jParams.getUser());
                JCRNodeWrapper node = s.getNode(path);
                if (!node.isVersioned()) {
                    node.versionFile();
                    s.save();
                }
            } catch (Throwable e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, ProcessingContext jParams) {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        try {
            VersionHistory vh = node.getVersionHistory();
            VersionIterator vi = vh.getAllVersions();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                if (!v.getName().equals("jcr:rootVersion")) {
                    JCRNodeWrapper orig = ((JCRVersionHistory) v.getContainingHistory()).getNode();
                    GWTJahiaNode n = ContentManagerHelper.getGWTJahiaNode(orig);
                    n.setUrl(orig.getUrl()+"?v="+v.getName());
                    GWTJahiaNodeVersion jahiaNodeVersion = new GWTJahiaNodeVersion(v.getName(), v.getCreated().getTime());
                    jahiaNodeVersion.setNode(n);
                    versions.add(jahiaNodeVersion);
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return versions;
    }

}
