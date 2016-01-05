/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.NodeIteratorImpl;
import org.jahia.services.content.VersionIteratorImpl;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Mar 18, 2009
 * Time: 4:26:42 PM
 * 
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

    public JCRVersion getRootVersion() throws RepositoryException {
        return (JCRVersion) getProvider().getNodeWrapper((Node) getRealNode().getRootVersion(), (JCRSessionWrapper) getSession());
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

    public JCRVersion getVersion(String s) throws VersionException, RepositoryException {
        return (JCRVersion) getProvider().getNodeWrapper((Node) getRealNode().getVersion(s), (JCRSessionWrapper) getSession());
    }

    public JCRVersion getVersionByLabel(String s) throws RepositoryException {
        return (JCRVersion) getProvider().getNodeWrapper((Node) getRealNode().getVersionByLabel(s), (JCRSessionWrapper) getSession());
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

    public String getVersionableIdentifier() throws RepositoryException {
        return getRealNode().getVersionableIdentifier();
    }

    public VersionIterator getAllLinearVersions() throws RepositoryException {
        VersionIterator vi = getRealNode().getAllLinearVersions();
        List l = new ArrayList();
        while (vi.hasNext()) {
            l.add((Version) getProvider().getNodeWrapper((Node) vi.nextVersion(), (JCRSessionWrapper) getSession()));
        }
        return new VersionIteratorImpl(l.iterator(), l.size());
    }

    public NodeIterator getAllLinearFrozenNodes() throws RepositoryException {
        NodeIterator vi = getRealNode().getAllLinearFrozenNodes();
        List l = new ArrayList();
        while (vi.hasNext()) {
            l.add(getProvider().getNodeWrapper(vi.nextNode(), (JCRSessionWrapper) getSession()));
        }
        return new NodeIteratorImpl(l.iterator(), l.size());
    }

    public NodeIterator getAllFrozenNodes() throws RepositoryException {
        NodeIterator vi = getRealNode().getAllFrozenNodes();
        List l = new ArrayList();
        while (vi.hasNext()) {
            l.add(getProvider().getNodeWrapper(vi.nextNode(), (JCRSessionWrapper) getSession()));
        }
        return new NodeIteratorImpl(l.iterator(), l.size());
    }

    @Override
    public Lock getLock()
            throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        throw new LockException("VersionHistory node are not lockable");
    }
}
