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
package org.jahia.services.content.impl.exo;

import org.apache.log4j.Logger;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.jahia.services.content.JCRStoreProvider;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeManager;
import java.io.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 30 nov. 2007
 * Time: 12:04:23
 * To change this template use File | Settings | File Templates.
 */
public class ExoStoreProvider extends JCRStoreProvider {
    
    private static final transient Logger logger = Logger.getLogger(ExoStoreProvider.class);
    
    protected void registerCustomNodeTypes(Workspace ws) throws IOException, RepositoryException {
        NodeTypeManager tm = ws.getNodeTypeManager();

        try {
            String path = org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/exo/jahia-nodetypes.xml";
            Method m = tm.getClass().getMethod("registerNodeTypes", new Class[]{InputStream.class, Integer.TYPE});
            m.invoke(tm, new Object[] {new FileInputStream(path), new Integer(ExtendedNodeTypeManager.IGNORE_IF_EXISTS)});
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
