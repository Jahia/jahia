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
import org.jahia.services.content.JCRSessionWrapper;
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
    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(JackrabbitStoreProvider.class);

    public JackrabbitStoreProvider() {
    }                 

    @Override
    public void start() throws JahiaInitializationException {
        boolean liveWorkspaceCreated = false;
        JCRSessionWrapper session = null;
        JCRSessionWrapper livesession = null;
        try {
            try {
                session = getSystemSession(null, Constants.LIVE_WORKSPACE);
                session.getProviderSession(this);
            } catch (NoSuchWorkspaceException e) {
                session = getSystemSession();
                JackrabbitWorkspace jrWs = (JackrabbitWorkspace) session.getProviderSession(this)
                        .getWorkspace();
                jrWs.createWorkspace(Constants.LIVE_WORKSPACE);
                liveWorkspaceCreated = true;
            } finally {
                session.logout();
            }
            super.start();
            if (liveWorkspaceCreated) {
                session = getSystemSession();
                Session providerSession = session.getProviderSession(this);
                Node n = providerSession.getNode("/");
                recurseCheckin(n, providerSession.getWorkspace().getVersionManager());
                NodeIterator ni = n.getNodes();
                livesession = getSystemSession(null, Constants.LIVE_WORKSPACE);

                Session liveProviderSession = livesession.getProviderSession(this);
                Node liveRootNode = liveProviderSession.getRootNode();
                if (!liveRootNode.isNodeType(Constants.MIX_REFERENCEABLE)) {
                    liveRootNode.addMixin(Constants.MIX_REFERENCEABLE);
                    livesession.save();
                }

                while (ni.hasNext()) {
                    Node node = (Node) ni.next();
                    if (!node.getName().equals("jcr:system")) {
                        if (!liveProviderSession.nodeExists(node.getPath())) {
                            liveProviderSession.getWorkspace().clone("default", node.getPath(), node.getPath(), false);
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
            versionManager.checkpoint(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            recurseCheckin(sub, versionManager);
        }
    }

    public void stop() {
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);
        long timer = System.currentTimeMillis();
        NodeTypeManager jntm  = ws.getNodeTypeManager();
        NamespaceRegistry namespaceRegistry = ws.getNamespaceRegistry();
        List<NodeTypeDefinition> nts = new ArrayList<NodeTypeDefinition>();
        while (nti.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
            String uri = nodeType.getNameObject().getUri();
            if (!Name.NS_NT_URI.equals(uri) && !Name.NS_MIX_URI.equals(uri) && !Name.NS_REP_URI.equals(uri)) {
                try {
                    namespaceRegistry.getURI(nodeType.getNameObject().getPrefix());
                } catch (NamespaceException e) {
                    namespaceRegistry.registerNamespace(nodeType.getNameObject().getPrefix(), uri);
                }
                nts.add(nodeType.getNodeTypeDefinition());
            }
        }
        jntm.registerNodeTypes(nts.toArray(new NodeTypeDefinition[nts.size()]), true);
        logger.info("Custom node types registered for " +systemId + " in " + (System.currentTimeMillis() - timer) + " ms");
    }

    protected boolean canRegisterCustomNodeTypes() {
        return true;
    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        Node rootNode = session.getRootNode();
//        JCRNodeWrapperImpl.changeRoles(rootNode,"g:guest","r----");
//        JCRNodeWrapperImpl.changeRoles(rootNode,"g:users","re---");
    }

}
