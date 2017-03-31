/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.console.Session;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.UUID;

public class JCRCommandSupport {

    public static String WORKSPACE = "JcrCommand.WORKSPACE";
    public static String PATH = "JcrCommand.PATH";

    protected String getCurrentPath(Session session) {
        String path = (String) session.get(PATH);
        if (path == null) {
            path = "/";
            setCurrentPath(session, path);
        }
        return path;
    }

    protected void setCurrentPath(Session session, String path) {
        session.put(PATH, path);
    }

    protected String getCurrentWorkspace(Session session) {
        String workspace = (String) session.get(WORKSPACE);
        if (workspace == null) {
            workspace = "default";
            setCurrentWorkspace(session, workspace);
        }
        return workspace;
    }

    protected void setCurrentWorkspace(Session session, String workspace) {
        session.put(WORKSPACE, workspace);
    }

    protected JCRNodeWrapper getNode(JCRSessionWrapper jcrsession, String path, Session session) throws RepositoryException {
        if (path == null) {
            return jcrsession.getNode(getCurrentPath(session));
        } else if (path.startsWith("/")) {
            return jcrsession.getNode(path);
        } else if (path.equals("..")) {
            JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
            return n.getParent();
        } else {
            try {
                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                return n.getNode(path);
            } catch (PathNotFoundException e) {
                try {
                    // Try with UUID
                    UUID.fromString(path);
                    return jcrsession.getNodeByIdentifier(path);
                } catch (IllegalArgumentException e1) {
                    throw e;
                }
            }
        }
    }

}
