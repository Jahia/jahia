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


    public String encodeInternalName(String name) {
        return name;
    }

    public String decodeInternalName(String name) {
        return name;
    }

    protected void registerNamespaces(Workspace workspace) throws RepositoryException {
    }

    public boolean isExportable() {
        return false;
    }
}
