/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.jahia;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;

import org.apache.log4j.Logger;
import org.jahia.jaas.JahiaLoginModule;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:03:56
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryImpl implements Repository {

    private static final transient Logger logger = Logger.getLogger(RepositoryImpl.class);

    private JahiaUserManagerService userService;
    private JahiaGroupManagerService groupService;
    private JahiaSitesService sitesService;
    private ProcessingContextFactory processingContextFactory;

    public RepositoryImpl() {
    }

    public String[] getDescriptorKeys() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDescriptor(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @todo Only SimpleCredentials are supported in the current implementation.
     * @param credentials
     * @param workspaceName
     * @return
     * @throws LoginException
     * @throws NoSuchWorkspaceException
     * @throws RepositoryException
     */
    public Session login(Credentials credentials, String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("Only SimpleCredentials supported in this implementation");
        }
        SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
        String key = simpleCredentials.getUserID();
        JahiaUser jahiaUser;

        if (key.startsWith(JahiaLoginModule.SYSTEM)) {
            jahiaUser = groupService.getAdminUser(0);
        } else if (key.equals(JahiaLoginModule.GUEST)) {
            jahiaUser = userService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
        } else {
            jahiaUser = userService.lookupUser(key);
        }

        if (jahiaUser == null) {
            throw new LoginException("User " + simpleCredentials.getUserID() + " not found");
        }
//        if (!ServicesRegistry.getInstance().getJahiaUserManagerService().login(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()))) {
//            throw new LoginException("User " + simpleCredentials.getUserID() + " couldn't login, invalid password ?");
//        }

        SessionImpl newSession = new SessionImpl(this, jahiaUser, key);
        Workspace workspace = new WorkspaceImpl(newSession, workspaceName);
        newSession.setWorkspace(workspace);
        return newSession;
    }

    public Session login(Credentials credentials) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(credentials, null);
    }

    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(null, workspaceName);
    }

    public Session login() throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(null, null);
    }

    public JahiaUserManagerService getUserService() {
        return userService;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    public JahiaGroupManagerService getGroupService() {
        return groupService;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public JahiaSitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public ProcessingContextFactory getProcessingContextFactory() {
        return processingContextFactory;
    }

    public void setProcessingContextFactory(ProcessingContextFactory processingContextFactory) {
        this.processingContextFactory = processingContextFactory;
    }
}
