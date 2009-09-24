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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.spi.Path;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;

import javax.jcr.*;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 nov. 2007
 * Time: 15:30:18
 * To change this template use File | Settings | File Templates.
 */
public class JackrabbitNodeWrapper extends JCRNodeWrapperImpl {
    private static final transient Logger logger = Logger.getLogger(JackrabbitNodeWrapper.class);

    protected JackrabbitNodeWrapper(Node objectNode, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(objectNode, session, provider);
    }

    @Override
    public boolean hasPermission(String perm) {
        if (exception != null) {
            return false;
        }
        String ws = null;
        int permissions = 0;
        try {
            if (READ.equals(perm)) {
                permissions = Permission.READ;
                ws = "default";
            } else if (WRITE.equals(perm)) {
                permissions = Permission.ADD_NODE;
                ws = "default";
            } else if (MODIFY_ACL.equals(perm)) {
                permissions = Permission.MODIFY_AC;
                ws = "default";
            } else if (READ_LIVE.equals(perm)) {
                permissions = Permission.READ;
                ws = "live";
            } else if (WRITE_LIVE.equals(perm)) {
                permissions = Permission.ADD_NODE;
                ws = "live";
            }
            if (ws == null) {
                return false;
            }
            if (ws.equals(workspace.getName())) {
                SessionImpl jrSession = (SessionImpl) session.getProviderSession(provider);
                Path path = jrSession.getQPath(localPath).getNormalizedPath();
                return jrSession.getAccessManager().isGranted(path, permissions);
            } else {
                SessionImpl jrSession = (SessionImpl) provider.getThreadSession(getUser(), "live");
                Node current = this;
                while (true) {
                    try {
                        Path path = jrSession.getQPath(current.getCorrespondingNodePath(ws)).getNormalizedPath();
                        return jrSession.getAccessManager().isGranted(path, permissions);
                    } catch (ItemNotFoundException nfe) {
                        // corresponding node not found
                        try {
                            current = current.getParent();
                        } catch (AccessDeniedException e) {
                            return false;
                        } catch (ItemNotFoundException e) {
                            return false;
                        }
                    }
                }
            }
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
            return false;
        }
    }

    public Map<String, List<String[]>> getAclEntries() {
        try {
            Map<String, List<String[]>> permissions = new HashMap<String, List<String[]>>();
            Map<String, List<String[]>> inheritedPerms = new HashMap<String, List<String[]>>();

            recurseonACPs(permissions, inheritedPerms, objectNode);
            for (String s : inheritedPerms.keySet()) {
                if (permissions.containsKey(s)) {
                    List<String[]> l = permissions.get(s);
                    l.addAll(inheritedPerms.get(s));
                } else {
                    permissions.put(s, inheritedPerms.get(s));
                }
            }
            return permissions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<String, Map<String, String>> getActualAclEntries() {
        Map<String, Map<String, String>> actualACLs = new HashMap<String, Map<String, String>>();
        Map<String, List<String[]>> allACLs = getAclEntries();
        if (allACLs != null) {
            for (Map.Entry<String, List<String[]>> entry : allACLs.entrySet()) {
                Map<String, String> permissionsForUser = new HashMap<String, String>();
                // filtering stuff (path, GRANT/DENY, jcr:perm)
                for (String[] perms : entry.getValue()) {
                    if (permissionsForUser.containsKey(perms[2])) {
                        if (perms[0].equals(getPath())) {
                            permissionsForUser.put(perms[2], perms[1]);
                        }
                    } else {
                        permissionsForUser.put(perms[2], perms[1]);
                    }
                }
                actualACLs.put(entry.getKey(), permissionsForUser);
            }
        }
        return actualACLs;
    }

    public boolean getAclInheritanceBreak() {
        if (exception != null) {
            return false;
        }
        try {
            return getAclInheritanceBreak(objectNode);
        } catch (RepositoryException e) {
            logger.error("Cannot get acl", e);
            return false;
        }
    }

    public boolean changePermissions(String user, String perm) {
        if (exception != null) {
            return false;
        }
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    public boolean changePermissions(String user, Map<String, String> perm) {
        if (exception != null) {
            return false;
        }
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    public boolean setAclInheritanceBreak(boolean inheritance) {
        if (exception != null) {
            return false;
        }
        try {
            setAclInheritanceBreak(objectNode, inheritance);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    public boolean revokePermissions(String user) {
        if (exception != null) {
            return false;
        }
        try {
            revokePermission(objectNode, user);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    public boolean revokeAllPermissions() {
        try {
            if (objectNode.hasNode("j:acl")) {
                objectNode.getNode("j:acl").remove();
                objectNode.removeMixin("jmix:accessControlled");
                return true;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    private void recurseonACPs(Map<String, List<String[]>> results, Map<String, List<String[]>> inherited, Node n) throws RepositoryException {
        try {
            Map<String, List<String[]>> current = results;
            while (true) {
                if (n.hasNode("j:acl")) {
                    Node acl = n.getNode("j:acl");
                    NodeIterator aces = acl.getNodes();
                    Map<String, List<String[]>> localResults = new HashMap<String, List<String[]>>();
                    while (aces.hasNext()) {
                        Node ace = aces.nextNode();
                        String principal = ace.getProperty("j:principal").getString();
                        String type = ace.getProperty("j:aceType").getString();
                        Value[] privileges = ace.getProperty("j:privileges").getValues();

                        if (!current.containsKey(principal)) {
                            List<String[]> p = localResults.get(principal);
                            if (p == null) {
                                p = new ArrayList<String[]>();
                                localResults.put(principal, p);
                            }
                            for (Value privilege : privileges) {
                                p.add(new String[]{n.getPath(), type, privilege.getString()});
                            }
                        }
                    }
                    current.putAll(localResults);
                    if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                        return;
                    }
                }
                n = n.getParent();
                current = inherited;
            }
        } catch (ItemNotFoundException e) {
            return;
        }
    }

}
