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
package org.jahia.services.history;

import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.version.InternalVersionHistory;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.spi.Name;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version history utility class for purging all version entries of the specified node.
 * 
 * @author Sergiy Shyrkov
 */
public final class NodeVersionHistoryHelper {

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryHelper.class);

    public static int purgeVersionHistoryForNode(final String nodeIdentifier) {
        int count = 0;
        try {
            count = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Integer>() {
                        public Integer doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            return purgeVersionHistoryForNode(nodeIdentifier, session);
                        }
                    });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return count;
    }

    private static int purgeVersionHistoryForNode(String nodeIdentifier,
            JCRSessionWrapper sessionWrapper) throws PathNotFoundException, RepositoryException {
        long timer = System.currentTimeMillis();
        int count = 0;
        SessionImpl session = (SessionImpl) sessionWrapper.getProviderSession(sessionWrapper
                .getNode("/").getProvider());
        InternalVersionManager vm = session.getInternalVersionManager();

        if (logger.isDebugEnabled()) {
            logger.debug("Start purging version history for node {}", nodeIdentifier);
        }
        InternalVersionHistory history = null;
        try {
            history = vm.getVersionHistoryOfNode(NodeId.valueOf(nodeIdentifier));
        } catch (ItemNotFoundException e) {
            // no history found
            return 0;
        }

        Name[] versionNames = history.getVersionNames();

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} versions", versionNames.length);
        }

        for (Name v : versionNames) {
            if (v.getLocalName().equals("rootVersion")) {
                continue;
            }
            try {
                vm.removeVersion(session, history, v);
                count++;
                if (logger.isDebugEnabled()) {
                    logger.debug("removed version {}", v.getLocalName());
                }
            } catch (ReferentialIntegrityException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping still referenced version {}", v.getLocalName());
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Purged {} version history items for node {} in {} ms",
                    new String[] { String.valueOf(count), nodeIdentifier,
                            String.valueOf(System.currentTimeMillis() - timer) });
        }

        return count;
    }

    public static int purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers) {
        int total = 0;
        try {
            total = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Integer>() {
                        public Integer doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            int count = 0;
                            for (String id : nodeIdentifiers) {
                                count += purgeVersionHistoryForNode(id, session);
                            }
                            return count;
                        }
                    });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return total;
    }

    private NodeVersionHistoryHelper() {
        super();
    }
}
