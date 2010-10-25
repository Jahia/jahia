/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

/*
 * Created on 10 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.spi.Name;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.*;
import javax.jcr.version.VersionManager;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeDefinition;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author hollis
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JackrabbitStoreProvider extends JCRStoreProvider {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JackrabbitStoreProvider.class);

    public JackrabbitStoreProvider() {
    }                 

    @Override
    public void start() throws JahiaInitializationException {
        boolean liveWorkspaceCreated = false;
        Session session = null;
        Session livesession = null;
        try {
            try {
                session = getSystemSession(null, Constants.LIVE_WORKSPACE);
            } catch (NoSuchWorkspaceException e) {
                session = getSystemSession();
                JackrabbitWorkspace jrWs = (JackrabbitWorkspace) session
                        .getWorkspace();
                jrWs.createWorkspace(Constants.LIVE_WORKSPACE);
                liveWorkspaceCreated = true;
                session.logout();
            }
            super.start();
            if (liveWorkspaceCreated) {
                session = getSystemSession();
                Node n = session.getNode("/");
                recurseCheckin(n, session.getWorkspace().getVersionManager());
                NodeIterator ni = n.getNodes();
                livesession = getSystemSession(null, Constants.LIVE_WORKSPACE);
                
                Node liveRootNode = livesession.getRootNode();
                if (!liveRootNode.isNodeType(Constants.MIX_REFERENCEABLE)) {
                    liveRootNode.addMixin(Constants.MIX_REFERENCEABLE);
                    livesession.save();
                }

                while (ni.hasNext()) {
                    Node node = (Node) ni.next();
                    if (!node.getName().equals("jcr:system")) {
                        if (!livesession.nodeExists(node.getPath())) {
                            livesession.getWorkspace().clone("default", node.getPath(), node.getPath(), false);
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error starting store provider", e);
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
            if (livesession != null && livesession.isLive()) {
                livesession.logout();
            }
        }
    }

    private void recurseCheckin(Node node, VersionManager versionManager) throws RepositoryException {
        if (node.isNodeType("mix:versionable")) {
            versionManager.checkin(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            recurseCheckin(sub, versionManager);
        }
    }

    public void stop() {
    }

    protected void registerCustomNodeTypes(Workspace ws) throws IOException, RepositoryException {
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getAllNodeTypes();
        registerCustomNodeTypes(nti, ws);
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);
        registerCustomNodeTypes(nti, ws);
    }

    private void registerCustomNodeTypes(NodeTypeIterator nti, Workspace ws) throws RepositoryException {
    	long timer = System.currentTimeMillis();
        NodeTypeManager jntm  = ws.getNodeTypeManager();
        NamespaceRegistry namespaceRegistry = ws.getNamespaceRegistry();
        List<NodeTypeDefinition> nts = new ArrayList<NodeTypeDefinition>();
        while (nti.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
            if (!Name.NS_NT_URI.equals(nodeType.getNameObject().getUri()) && !Name.NS_MIX_URI.equals(nodeType.getNameObject().getUri())) {
                try {
                    namespaceRegistry.getURI(nodeType.getNameObject().getPrefix());
                } catch (NamespaceException e) {
                    namespaceRegistry.registerNamespace(nodeType.getNameObject().getPrefix(), nodeType.getNameObject().getUri());
                }                
                nts.add(nodeType.getNodeTypeDefinition());
            }
        }
        jntm.registerNodeTypes(nts.toArray(new NodeTypeDefinition[nts.size()]), true);
        logger.info("registerCustomNodeTypes took " + (System.currentTimeMillis() - timer) + " ms");
    }

    protected boolean canRegisterCustomNodeTypes() {
        return true;
    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        Node rootNode = session.getRootNode();
        JCRNodeWrapperImpl.changePermissions(rootNode,"g:guest","r----");
        JCRNodeWrapperImpl.changePermissions(rootNode,"g:users","re---");
    }

}
