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
