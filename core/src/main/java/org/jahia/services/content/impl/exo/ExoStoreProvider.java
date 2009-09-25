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
            m.invoke(tm, new Object[] {new FileInputStream(path), ExtendedNodeTypeManager.IGNORE_IF_EXISTS});
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
