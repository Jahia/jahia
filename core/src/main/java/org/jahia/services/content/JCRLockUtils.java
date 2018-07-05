/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import java.util.ArrayList;
import java.util.List;

/**
 * Locks utils class
 */
class JCRLockUtils {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRLockUtils.class);

    static void checkLock(JCRNodeWrapper node, boolean unlockSharedLock, boolean forAdd) throws RepositoryException {
        JCRSessionWrapper session = node.getSession();
        if (!session.isSystem() && node.isLocked()) {

            Node realNode = node.getRealNode();
            List<String> owners = getLockOwners(realNode);
            if (realNode.hasProperty("j:locktoken") && (unlockSharedLock ||
                    (owners.size() == 1 && owners.contains(session.getUserID())) ||
                    (forAdd && getLockInfos(realNode).stream().allMatch(s->s.endsWith(JCRNodeLockType.ALLOWS_ADD_SUFFIX))))) {
                realNode.getSession().addLockToken(realNode.getProperty("j:locktoken").getString());
            } else {
                boolean lockOwningSession = false;
                if (owners.size() == 0) {
                    Lock lock = realNode.getLock();
                    lockOwningSession = lock != null && lock.isLockOwningSession();
                }
                if (!lockOwningSession) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Node " + realNode.getPath() + " locked. Locks info:" + getLockInfos(realNode));
                    }
                    throw new LockException("Node locked.");
                }
            }
            if (session.getLocale() != null && !forAdd) {
                try {
                    Node i18n = node.getI18N(session.getLocale());
                    if (i18n.isLocked()) {
                        owners = getLockOwners(i18n);
                        if (owners.size() == 1 && owners.contains(session.getUserID())) {
                            i18n.getSession().addLockToken(i18n.getProperty("j:locktoken").getString());
                        } else {
                            boolean lockOwningSession = false;
                            if (owners.size() == 0) {
                                Lock lock = i18n.getLock();
                                lockOwningSession = lock != null && lock.isLockOwningSession();
                            }
                            if (!lockOwningSession) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Node i18n " + i18n.getPath() + " locked. Locks info:" + getLockInfos(i18n));
                                }
                                throw new LockException("Node locked.");
                            }
                        }
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("checkLock : no i18n node for node {}", node.getCanonicalPath());
                }
            }
        }
    }

    static List<String> getLockOwners(Node node) throws RepositoryException {
        List<String> types = getLockInfos(node);

        List<String> r = new ArrayList<String>();
        for (String type : types) {
            String owner = StringUtils.substringBefore(type, ":");
            if (!r.contains(owner)) {
                r.add(owner);
            }
        }
        return r;
    }

    static List<String> getLockInfos(Node node) throws RepositoryException {
        List<String> r = new ArrayList<String>();
        Value[] values = null;
        try {
            values = node.getProperty("j:lockTypes").getValues();
        } catch (PathNotFoundException e) {
            // no j:lockTypes property found -> skipping
        }
        if (values != null) {
            for (Value value : values) {
                if (!r.contains(value.getString())) {
                    r.add(value.getString());
                }
            }
        }
        return r;
    }
}
