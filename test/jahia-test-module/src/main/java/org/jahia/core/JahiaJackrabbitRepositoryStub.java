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

package org.jahia.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.core.JackrabbitRepositoryStub;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.test.NotExecutableException;
import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

public class JahiaJackrabbitRepositoryStub extends RepositoryStub {

    /**
     * Repository settings.
     */
    private final Properties settings;

    public JahiaJackrabbitRepositoryStub(Properties settings) {
        super(getStaticProperties());
        // set some attributes on the sessions
        superuser.setAttribute("jackrabbit", "jackrabbit");
        readwrite.setAttribute("jackrabbit", "jackrabbit");
        readonly.setAttribute("jackrabbit", "jackrabbit");
        // Repository settings
        this.settings = getStaticProperties();
        this.settings.putAll(settings);
    }

    private static Properties getStaticProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = getResource("JackrabbitRepositoryStub.properties");
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            // TODO: Log warning
        }
        try {
            InputStream stream = getResource("JahiaJackrabbitRepositoryStub.properties");
            try {
                Properties jahiaProperties = new Properties();
                jahiaProperties.load(stream);
                properties.putAll(jahiaProperties);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            // TODO: Log warning
        }
        return properties;
    }

    private static InputStream getResource(String name) {
        InputStream is = JahiaJackrabbitRepositoryStub.class.getResourceAsStream(name);
        if (is == null) {
            is = JackrabbitRepositoryStub.class.getResourceAsStream(name);
        }
        return is;
    }

    @Override
    public synchronized Repository getRepository() throws RepositoryStubException {
        Repository repository = JCRSessionFactory.getInstance();
        if (repository == null) {
            throw new RepositoryStubException("Failed to start repository");
        } else {
            try {
                Session session = JCRSessionFactory.getInstance().getCurrentUserSession();
                try {
                    if (!isTestWorkspacePrepared(session)) {
                        prepareTestContent(session);
                    } 
                } finally {
                }
            } catch (Exception e) {
                RepositoryStubException exception = new RepositoryStubException(
                        "Failed to start repository");
                exception.initCause(e);
                throw exception;
            } finally {
                //JCRSessionFactory.getInstance().setCurrentUser(null);
            }
        }
        return repository;
    }

    private boolean isTestWorkspacePrepared(Session session) throws RepositoryException,
            IOException {
        boolean workspacePrepared = false;

        try {
            if (session.getRootNode().getNode("testdata") != null) {
                workspacePrepared = true;
            }
        } catch (PathNotFoundException e) {
        }
        return workspacePrepared;
    }

    private void prepareTestContent(Session session) throws RepositoryException, IOException, org.jahia.services.content.nodetypes.ParseException {
        JahiaUser readOnlyUser = JCRUserManagerProvider.getInstance().lookupUser(readonly.getUserID());
        if (readOnlyUser == null) {
            readOnlyUser = JCRUserManagerProvider.getInstance().createUser(readonly.getUserID(), new String(readonly.getPassword()), new Properties());
            ((JCRSessionWrapper)session).getRootNode().grantRoles("u:"+readonly.getUserID(), Collections.singleton("privileged"));
            JahiaGroup usersGroup = JCRGroupManagerProvider.getInstance().lookupGroup(JahiaGroupManagerService.USERS_GROUPNAME);
            usersGroup.addMember(readOnlyUser);
        }
        
        JahiaTestContentLoader loader = new JahiaTestContentLoader();
        loader.loadTestContent(session);
    }

    @Override
    public Principal getKnownPrincipal(Session session) throws RepositoryException {

        Principal knownPrincipal = null;

        if (session instanceof SessionImpl) {
            for (Principal p : ((SessionImpl) session).getSubject().getPrincipals()) {
                if (!(p instanceof Group)) {
                    knownPrincipal = p;
                }
            }
        }

        if (knownPrincipal != null) {
            return knownPrincipal;
        } else {
            throw new RepositoryException("no applicable principal found");
        }
    }

    private static Principal UNKNOWN_PRINCIPAL = new Principal() {
        public String getName() {
            return "an_unknown_user";
        }
    };

    @Override
    public Principal getUnknownPrincipal(Session session) throws RepositoryException,
            NotExecutableException {
        return UNKNOWN_PRINCIPAL;
    }
}
