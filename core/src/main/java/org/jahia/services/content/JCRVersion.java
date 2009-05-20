package org.jahia.services.content;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 16, 2009
 * Time: 3:51:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRVersion extends JCRNodeDecorator implements Version {
    public JCRVersion(JCRNodeWrapper node) {
        super(node);
    }

    public Version getRealNode() {
        return (Version) super.getRealNode();
    }

    public JCRVersionHistory getContainingHistory() throws RepositoryException {
        return (JCRVersionHistory) getProvider().getNodeWrapper((Node) getRealNode().getContainingHistory(), (JCRSessionWrapper) getSession());
    }

    public Calendar getCreated() throws RepositoryException {
        return getRealNode().getCreated();
    }

    public Version[] getSuccessors() throws RepositoryException {
        Version[] versions = getRealNode().getSuccessors();
        for (int i = 0; i < versions.length; i++) {
            versions[i] = (Version) getProvider().getNodeWrapper((Node) versions[i], (JCRSessionWrapper) getSession());
        }
        return versions;
    }

    public Version[] getPredecessors() throws RepositoryException {
        Version[] versions = getRealNode().getPredecessors();
        for (int i = 0; i < versions.length; i++) {
            versions[i] = (Version) getProvider().getNodeWrapper((Node) versions[i], (JCRSessionWrapper) getSession());
        }
        return versions;
    }
}
