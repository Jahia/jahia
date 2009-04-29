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
package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.JCRStoreProvider;
import org.jahia.params.ProcessingContextFactory;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;

import javax.jcr.Repository;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1 févr. 2008
 * Time: 10:51:11
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContentStoreProvider extends JCRStoreProvider {

    private ProcessingContextFactory processingContextFactory;

    public synchronized Repository getRepository(){
        if (repo == null) {
            RepositoryImpl repoImpl = new RepositoryImpl();

            repoImpl.setSitesService(getSitesService());
            repoImpl.setGroupService(getGroupManagerService());
            repoImpl.setUserService(getUserManagerService());
            repoImpl.setProcessingContextFactory(processingContextFactory);
            repo = repoImpl;

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

    public void setProcessingContextFactory(ProcessingContextFactory processingContextFactory) {
        this.processingContextFactory = processingContextFactory;
    }

    public boolean isExportable() {
        return false;
    }


}
