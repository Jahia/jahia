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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.jcr;

import org.apache.karaf.shell.api.console.Session;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.UUID;

/**
 * Base class for JCR Commands
 */
public class JCRCommandSupport {

    public static final String WORKSPACE = "JcrCommand.WORKSPACE";
    public static final String PATH = "JcrCommand.PATH";

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
