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
package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Helper class to simplify and unify JCR data access.<br/>
 * <br/>
 * The template is taking care of properly opening and closing sessions, so it does not
 * need to be done by the callback actions.<br/>
 * <br/>
 * Data access or business logic service should rather use this template
 * than managing sessions by themselves.<br/>
 * <br/>
 * Requires a {@link JCRSessionFactory} to provide access to a JCR repository.
 *
 * @author Cedric Mailleux
 */
public class JCRTemplate implements org.jahia.api.content.JCRTemplate {

    private JCRSessionFactory sessionFactory;

    private static volatile JCRTemplate instance;

    private JCRTemplate() {
    }

    /**
     * Obtain the JCRTemplate singleton
     *
     * @return the JCRTemplate singleton instance
     */
    public static JCRTemplate getInstance() {
        if (instance == null) {
            synchronized (JCRTemplate.class) {
                if (instance == null) {
                    instance = new JCRTemplate();
                }
            }
        }
        return instance;
    }

    /**
     * @param sessionFactory The sessionFactory to set.
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public <X> X doExecuteWithSystemSessionInSameWorkspaceAndLocale(JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = getSessionFactory().getCurrentUserSession();
        if (session == null) {
            throw new RepositoryException("Trying to execute as a system session using current workspace and locale when no current user session exists !");
        }
        return doExecuteWithSystemSessionAsUser(null, session.getWorkspace().getName(), session.getLocale(), callback);
    }

    @Override
    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSessionAsUser(null, null, null, callback);
    }

    /**
     * @deprecated Use doExecuteWithSystemSession with JahiaUser
     */
    @Deprecated
    public <X> X doExecuteWithSystemSession(String username, JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(username, null, null, callback);
    }

    /**
     * @deprecated Use doExecuteWithSystemSession with JahiaUser
     */
    @Deprecated
    public <X> X doExecuteWithSystemSession(String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(username, workspace, null, callback);
    }

    /**
     * @deprecated Use doExecuteWithSystemSession with JahiaUser
     */
    @Deprecated
    public <X> X doExecuteWithSystemSession(String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            if (username != null && username.startsWith(" system ")) {
                throw new IllegalArgumentException("the username cannot start by \" system \"");
            }
            session = sessionFactory.getSystemSession(username, null, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @Override
    public <X> X doExecuteWithLongSystemSession(JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithLongSystemSessionAsUser(null, null, null, callback);
    }

    @Override
    public <X> X doExecuteWithSystemSessionAsUser(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getSystemSession(user != null ? user.getUsername() : null, user != null ? user.getRealm() : null, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @Override
    public <X> X doExecuteWithLongSystemSessionAsUser(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getSystemSession(user != null ? user.getUsername() : null, user != null ? user.getRealm() : null, workspace, locale).disableSessionCache();
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * @deprecated Use doExecute with JahiaUser
     */
    @Deprecated
    public <X> X doExecuteWithUserSession(String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(username, null, workspace, null);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * @deprecated Use doExecute with JahiaUser
     */
    @Deprecated
    public <X> X doExecuteWithUserSession(String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(username, null, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @Override
    public <X> X doExecute(String username, String realm, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(username, realm, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @Override
    public <X> X doExecute(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(user != null ? user.getUsername() : null, user != null ? user.getRealm() : null, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * @deprecated Use doExecuteWithSystemSession or doExecute with JahiaUser
     */
    @Deprecated
    public <X> X doExecute(boolean useSystemSession, String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        if (useSystemSession) {
            return doExecuteWithSystemSession(username, workspace, locale, callback);
        } else {
            return doExecuteWithUserSession(username, workspace, locale, callback);
        }
    }

    /**
     * @deprecated Use doExecuteWithSystemSession or doExecute with JahiaUser
     */
    @Deprecated
    public <X> X doExecute(boolean useSystemSession, String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        if (useSystemSession) {
            return doExecuteWithSystemSession(username, workspace, callback);
        } else {
            return doExecuteWithUserSession(username, workspace, callback);
        }
    }

    @Override
    public <X> X doExecuteWithLongSession(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(user != null ? user.getUsername() : null, user != null ? user.getRealm() : null, workspace, locale).disableSessionCache();
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    public JCRStoreProvider getProvider(String path) {
        return sessionFactory.getProvider(path);
    }
}
