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
package org.jahia.services.content.impl.vfs;

import org.jahia.services.content.JCRStoreProvider;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;

import javax.jcr.Repository;
import javax.jcr.Workspace;
import javax.jcr.RepositoryException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSContentStoreProvider extends JCRStoreProvider {
    private Repository repo;

    private String root;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public synchronized Repository getRepository(){
        if (repo == null) {
            repo = new VFSRepositoryImpl(root);
            if (rmibind != null) {
                try {
                    Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
                } catch (MalformedURLException e) {
                } catch (RemoteException e) {
                }
            }
        }
        return repo;
    }

    protected void registerNamespaces(Workspace workspace) throws RepositoryException {
    }

    public boolean isExportable() {
        return false;
    }
}
