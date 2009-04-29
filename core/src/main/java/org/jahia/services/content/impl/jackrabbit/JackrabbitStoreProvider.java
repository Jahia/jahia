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
/*
 * Created on 10 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jahia.services.content.impl.jackrabbit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeIterator;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.JRCndWriter;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

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

        File cndOutFile = File.createTempFile("nodetypes",".cnd");
        cndOutFile.deleteOnExit();
        JRCndWriter w = new JRCndWriter(nti, NodeTypeRegistry.getInstance().getNamespaces(), new FileWriter(cndOutFile), ws.getNodeTypeManager());
        w.close();
        jntm.registerNodeTypes( new FileInputStream(cndOutFile), JackrabbitNodeTypeManager.TEXT_X_JCR_CND, true);
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        File cndOutFile = null;
        try {
            NodeTypeManagerImpl jntm  = (NodeTypeManagerImpl) ws.getNodeTypeManager();
            NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);

            cndOutFile = File.createTempFile("nodetypes",".cnd");
            JRCndWriter w = new JRCndWriter(nti, NodeTypeRegistry.getInstance().getNamespaces(), new FileWriter(cndOutFile), ws.getNodeTypeManager());
            w.close();
            jntm.registerNodeTypes( new FileInputStream(cndOutFile), JackrabbitNodeTypeManager.TEXT_X_JCR_CND, true);
            cndOutFile.delete();
        } catch (IOException e) {
            logger.error("Cannot parse file "+cndOutFile, e);
        }


    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        Node rootNode = session.getRootNode();
        Node node = rootNode.getNode(Constants.JCR_SYSTEM);
        // Import default ACLs
        if (!node.hasNode("j:acl")) {
            JCRNodeWrapperImpl.changePermissions(rootNode,"g:guest","r-");
        }
    }
}
