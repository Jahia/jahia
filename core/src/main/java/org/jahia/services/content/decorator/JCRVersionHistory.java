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

package org.jahia.services.content.decorator;

import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.VersionIteratorImpl;
import org.jahia.services.content.NodeIteratorImpl;

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
