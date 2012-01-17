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

package org.jahia.services.content.impl.vfs;

import static javax.jcr.security.Privilege.*;
import static org.jahia.api.Constants.EDIT_WORKSPACE;
import static org.jahia.api.Constants.LIVE_WORKSPACE;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.jackrabbit.commons.iterator.AccessControlPolicyIteratorAdapter;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRSessionFactory;

/**
 * Implementation of the {@link AccessControlManager} for the VFS provider.
 * 
 * @author Sergiy Shyrkov
 */
public class VFSAccessControlManager implements AccessControlManager {

    private static final AccessControlPolicy[] POLICIES = new AccessControlPolicy[0];

    private Privilege[] registeredPrivileges;
    
    private Privilege[] rootNodePrivileges;
    
    private Privilege[] privileges; 

    private Map<String, Privilege> registeredPrivilegeMap;

    private boolean readOnly;

    public VFSAccessControlManager(boolean readOnly) {
        super();
        this.readOnly = readOnly;
        try {
            init();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private void init() throws RepositoryException {
        JahiaPrivilegeRegistry registry = new JahiaPrivilegeRegistry(JCRSessionFactory.getInstance().getNamespaceRegistry());
        registeredPrivileges = registry.getRegisteredPrivileges();
        registeredPrivilegeMap = new HashMap<String, Privilege>();
        for (Privilege priv : registeredPrivileges) {
            registeredPrivilegeMap.put(priv.getName(), priv);
        }
        if (readOnly) {
            rootNodePrivileges = new Privilege[] {
                    privilegeFromName(JCR_READ + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_READ + "_" + LIVE_WORKSPACE),
                    };
            privileges = rootNodePrivileges;
        } else {
            rootNodePrivileges = new Privilege[] {
                    privilegeFromName(JCR_READ + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_READ + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_WRITE + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_WRITE + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_ADD_CHILD_NODES + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_ADD_CHILD_NODES + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_REMOVE_CHILD_NODES + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_REMOVE_CHILD_NODES + "_" + LIVE_WORKSPACE),
                    };
            privileges = new Privilege[] {
                    privilegeFromName(JCR_READ + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_READ + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_WRITE + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_WRITE + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_REMOVE_NODE + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_REMOVE_NODE + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_ADD_CHILD_NODES + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_ADD_CHILD_NODES + "_" + LIVE_WORKSPACE),
                    privilegeFromName(JCR_REMOVE_CHILD_NODES + "_" + EDIT_WORKSPACE), privilegeFromName(JCR_REMOVE_CHILD_NODES + "_" + LIVE_WORKSPACE),
                    };
        }
    }

    public AccessControlPolicyIterator getApplicablePolicies(String absPath)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return AccessControlPolicyIteratorAdapter.EMPTY;
    }

    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException {
        return POLICIES;
    }

    public AccessControlPolicy[] getPolicies(String absPath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException {
        return POLICIES;
    }

    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException,
            RepositoryException {
        return absPath.length() == 1 && "/".equals(absPath) ? rootNodePrivileges : privileges;
    }

    public Privilege[] getSupportedPrivileges(String absPath) throws PathNotFoundException,
            RepositoryException {
        return registeredPrivileges;
    }

    public boolean hasPrivileges(String absPath, Privilege[] privileges)
            throws PathNotFoundException, RepositoryException {
        if (privileges == null || privileges.length == 0) {
            return true;
        }
        boolean allowed = true;
        Privilege[] granted = getPrivileges(absPath);
        for (Privilege toCheck : privileges) {
            if (toCheck != null && !ArrayUtils.contains(granted, toCheck)) {
                allowed = false;
                break;
            }
        }

        return allowed;
    }

    public Privilege privilegeFromName(String privilegeName) throws AccessControlException,
            RepositoryException {
        return registeredPrivilegeMap.get(privilegeName);
    }

    public void removePolicy(String absPath, AccessControlPolicy policy)
            throws PathNotFoundException, AccessControlException, AccessDeniedException,
            LockException, VersionException, RepositoryException {
        // not supported
    }

    public void setPolicy(String absPath, AccessControlPolicy policy) throws PathNotFoundException,
            AccessControlException, AccessDeniedException, LockException, VersionException,
            RepositoryException {
        // not supported
    }

}
