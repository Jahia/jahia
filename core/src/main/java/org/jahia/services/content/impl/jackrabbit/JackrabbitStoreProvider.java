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
/*
 * Created on 10 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.nodetypes.JRCndWriter;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.*;
import javax.jcr.nodetype.NodeTypeIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
        try {
            try {
                session = getSystemSession(null, "live");
            } catch (NoSuchWorkspaceException e) {
                session = getSystemSession();
                JackrabbitWorkspace jrWs = (JackrabbitWorkspace) session
                        .getWorkspace();
                jrWs.createWorkspace("live");
                liveWorkspaceCreated = true;
                session.logout();
            }
            super.start();
            if (liveWorkspaceCreated) {
                session = getSystemSession(null, "live");
                session.getWorkspace().clone("default",
                        "/content", "/content", false);
                session.logout();
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            if(session!=null && session.isLive()) {
                session.logout();
            }
        }
    }

    public void stop() {
    }

    public JCRNodeWrapper getNodeWrapper(String path, JCRSessionWrapper session) {
        return getService().decorate(new JackrabbitNodeWrapper(path, session, this));
    }

    public JCRNodeWrapper getNodeWrapper(Node objectNode, JCRSessionWrapper session) {
        return getService().decorate(new JackrabbitNodeWrapper(objectNode, session, this));
    }

    protected void registerCustomNodeTypes(Workspace ws) throws IOException, RepositoryException {
        NodeTypeManagerImpl jntm  = (NodeTypeManagerImpl) ws.getNodeTypeManager();
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getAllNodeTypes();
        List<ExtendedNodeType> nts = new ArrayList<ExtendedNodeType>();
        while (nti.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
            if (!Constants.NT_NS.equals(nodeType.getNameObject().getUri()) && !Constants.MIX_NS.equals(nodeType.getNameObject().getUri())) {
                // todo : remove temporary hack to avoid re-registration of nodes
                try {
                    if (!jntm.hasNodeType(nodeType.getName())) {
                    nts.add(nodeType);
                    }
                } catch (RepositoryException e) {
                    nts.add(nodeType);
                }
            }
        }
        File cndOutFile = File.createTempFile("nodetypes",".cnd");
        cndOutFile.deleteOnExit();
        JRCndWriter w = new JRCndWriter(nts, NodeTypeRegistry.getInstance().getNamespaces(), new FileWriter(cndOutFile), ws.getNodeTypeManager());
        w.close();
        jntm.registerNodeTypes( new FileInputStream(cndOutFile), JackrabbitNodeTypeManager.TEXT_X_JCR_CND, true);
    }

    protected boolean canRegisterCustomNodeTypes() {
        return true;
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        File cndOutFile = null;
        try {
            NodeTypeManagerImpl jntm  = (NodeTypeManagerImpl) ws.getNodeTypeManager();
            NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);
            List<ExtendedNodeType> nts = new ArrayList<ExtendedNodeType>();
            while (nti.hasNext()) {
                ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
                if (!Constants.NT_NS.equals(nodeType.getNameObject().getUri()) && !Constants.MIX_NS.equals(nodeType.getNameObject().getUri())) {
                    // todo : remove temporary hack to avoid re-registration of nodes
                    try {
                        if (!jntm.hasNodeType(nodeType.getName())) {
                        nts.add(nodeType);
                        }
                    } catch (RepositoryException e) {
                        nts.add(nodeType);
                    }
                }
            }

            cndOutFile = File.createTempFile("nodetypes",".cnd");
            JRCndWriter w = new JRCndWriter(nts, NodeTypeRegistry.getInstance().getNamespaces(), new FileWriter(cndOutFile), ws.getNodeTypeManager());
            w.close();
            jntm.registerNodeTypes( new FileInputStream(cndOutFile), JackrabbitNodeTypeManager.TEXT_X_JCR_CND, true);
//            cndOutFile.delete();
        } catch (IOException e) {
            logger.error("Cannot parse file "+cndOutFile, e);
        }


    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        Node rootNode = session.getRootNode();
        Node node = rootNode.getNode(Constants.JCR_SYSTEM);
        // Import default ACLs
        if (!node.hasNode("j:acl")) {
            JCRNodeWrapperImpl.changePermissions(rootNode,"g:guest","r----");
            JCRNodeWrapperImpl.changePermissions(rootNode,"g:users","re---");
        }
    }

}
