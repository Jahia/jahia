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

    public Node getFrozenNode() throws RepositoryException {
        return getProvider().getNodeWrapper(getRealNode().getFrozenNode(), (JCRSessionWrapper) getSession());
    }

    public Version getLinearSuccessor() throws RepositoryException {
        return (Version) getProvider().getNodeWrapper(getRealNode().getLinearSuccessor(), (JCRSessionWrapper) getSession());
    }

    public Version getLinearPredecessor() throws RepositoryException {
        return (Version) getProvider().getNodeWrapper(getRealNode().getLinearPredecessor(), (JCRSessionWrapper) getSession());
    }
}
