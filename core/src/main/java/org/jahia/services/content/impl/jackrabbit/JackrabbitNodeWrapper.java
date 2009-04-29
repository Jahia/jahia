/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 nov. 2007
 * Time: 15:30:18
 * To change this template use File | Settings | File Templates.
 */
public class JackrabbitNodeWrapper extends JCRNodeWrapperImpl {
    private static final transient Logger logger = Logger.getLogger(JackrabbitNodeWrapper.class);

    protected JackrabbitNodeWrapper(String localPath, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(localPath, session, provider);
    }

    protected JackrabbitNodeWrapper(Node objectNode, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(objectNode, session, provider);
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

    public boolean getAclInheritanceBreak(){
        if (exception != null) {
            return false;
        }
        try {
            return getAclInheritanceBreak(objectNode);
        } catch (RepositoryException e) {
            logger.error("Cannot get acl",e);
            return false;
        }
    }

    public boolean changePermissions (String user, String perm) {
        if (exception != null) {
            return false;
        }
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl",e);
            return false;
        }

        return true;
    }

    public boolean changePermissions (String user, Map<String,String> perm) {
        if (exception != null) {
            return false;
        }
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl",e);
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
            logger.error("Cannot change acl",e);
            return false;
        }

        return true;
    }

    public boolean revokePermissions (String user) {
        if (exception != null) {
            return false;
        }
        try {
            revokePermission(objectNode, user);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl",e);
            return false;
        }

        return true;
    }

    public boolean revokeAllPermissions () {
        try {
            if (objectNode.hasNode("j:acl")) {
                objectNode.getNode("j:acl").remove();
                return true;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    private void recurseonACPs(Map<String, List<String[]>> results, Map<String, List<String[]>> inherited, Node n) throws RepositoryException  {
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
                            if (p == null)  {
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
