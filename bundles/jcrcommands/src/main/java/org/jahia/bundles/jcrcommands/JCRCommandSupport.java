package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.console.Session;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

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
            JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
            return n.getNode(path);
        }
    }

}
