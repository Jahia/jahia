package org.jahia.services.content;

import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionException;
import javax.jcr.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 18, 2009
 * Time: 4:26:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRVersionHistory extends JCRNodeDecorator implements VersionHistory {
    public JCRVersionHistory(JCRNodeWrapper node) {
        super(node);
    }

    public VersionHistory getRealNode() {
        return (VersionHistory) super.getRealNode();
    }

    public JCRNodeWrapper getNode() throws RepositoryException {
        PropertyIterator pi = getRealNode().getReferences();
        if (pi.hasNext()) {
            Property p = pi.nextProperty();
            return getProvider().getNodeWrapper(p.getParent(), (JCRSessionWrapper) getSession());
        }
        throw new ItemNotFoundException();
    }

    public String getVersionableUUID() throws RepositoryException {
        return getRealNode().getVersionableUUID();
    }

    public Version getRootVersion() throws RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getRootVersion(), (JCRSessionWrapper) getSession());
    }

    public VersionIterator getAllVersions() throws RepositoryException {
        VersionIterator all = getRealNode().getAllVersions();
        List l = new ArrayList();
        while (all.hasNext()) {
            Version v = all.nextVersion();
            l.add(getProvider().getNodeWrapper(v, (JCRSessionWrapper) getSession()));
        }
        return new VersionIteratorImpl(l.iterator(), l.size());
    }

    public Version getVersion(String s) throws VersionException, RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getVersion(s), (JCRSessionWrapper) getSession());
    }

    public Version getVersionByLabel(String s) throws RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getVersionByLabel(s), (JCRSessionWrapper) getSession());
    }

    public void addVersionLabel(String s, String s1, boolean b) throws VersionException, RepositoryException {
        getRealNode().addVersionLabel(s, s1, b);
    }

    public void removeVersionLabel(String s) throws VersionException, RepositoryException {
        getRealNode().removeVersionLabel(s);
    }

    public boolean hasVersionLabel(String s) throws RepositoryException {
        return getRealNode().hasVersionLabel(s);
    }

    public boolean hasVersionLabel(Version version, String s) throws VersionException, RepositoryException {
        return getRealNode().hasVersionLabel(((JCRVersion)version).getRealNode(), s);
    }

    public String[] getVersionLabels() throws RepositoryException {
        return getRealNode().getVersionLabels();
    }

    public String[] getVersionLabels(Version version) throws VersionException, RepositoryException {
        return getRealNode().getVersionLabels(((JCRVersion)version).getRealNode());
    }

    public void removeVersion(String s) throws ReferentialIntegrityException, AccessDeniedException, UnsupportedRepositoryOperationException, VersionException, RepositoryException {
        getRealNode().removeVersion(s);
    }
}
