/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.core;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.test.NotExecutableException;
import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Properties;

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
            is = RepositoryImpl.class.getResourceAsStream(name);
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
        JCRUserNode readOnlyUser = JahiaUserManagerService.getInstance().lookupUser(readonly.getUserID());
        if (readOnlyUser == null) {
            readOnlyUser = JahiaUserManagerService.getInstance().createUser(readonly.getUserID(), new String(readonly.getPassword()), new Properties(), (JCRSessionWrapper) session);
            ((JCRSessionWrapper)session).getRootNode().grantRoles("u:"+readOnlyUser.getName(), Collections.singleton("privileged"));
            JCRGroupNode usersGroup = JahiaGroupManagerService.getInstance().lookupGroupByPath(JahiaGroupManagerService.USERS_GROUPPATH);
            usersGroup.addMember(readOnlyUser);
            session.save();
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
