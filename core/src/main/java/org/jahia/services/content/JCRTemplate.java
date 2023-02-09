/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
 * Helper class to simplify and unify JCR data access.
 * <p/>
 * The template is taking care of properly opening and closing sessions, so it does not
 * need to be done by the callback actions.
 * <p/>
 * Data access or business logic service should rather use this template
 * than managing sessions by themselves.
 * <p/>
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

    /**
     * @return Returns the sessionFactory.
     */
    @Override
    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Execute the action specified by the given callback object within a system Session.
     * <p/>
     * The workspace and locale will be extracted by the current user session. This method assumes a current session
     * is available, and will generate a RepositoryException if there is no current session.
     * @param callback callback the <code>JCRCallback</code> that executes the client
     *                 operation
     * @param <X> the resulting object to return from the callback.
     * @return a result object returned by the action, or null
     * @throws RepositoryException if the method could not find a current user session, or if any other underlying
     * JCR error occurred.
     */
    @Override
    public <X> X doExecuteWithSystemSessionInSameWorkspaceAndLocale(JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = getSessionFactory().getCurrentUserSession();
        if (session == null) {
            throw new RepositoryException("Trying to execute as a system session using current workspace and locale when no current user session exists !");
        }
        return doExecuteWithSystemSessionAsUser(null, session.getWorkspace().getName(), session.getLocale(), callback);
    }

    /**
     * Execute the action specified by the given callback object within a system Session.
     * <p/>
     * The workspace logged into will be the repository's default workspace. The user
     * will be the current user of the thread obtained by JcrSessionFilter.getCurrentUser().
     * The locale will be "default".
     *
     * @param callback the <code>JCRCallback</code> that executes the client
     *                 operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
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

    /**
     * Execute the action specified by the given callback object within a system Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param user           the user to open the session with
     * @param workspace      the workspace name to log into
     * @param locale         the locale of the session, null to use unlocalized session
     * @param callback       the <code>JCRCallback</code> that executes the client
     *                       operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
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

    /**
     * Execute the action specified by the given callback object within a new user Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param user      the user to open the session with
     * @param workspace the workspace name to log into
     * @param locale    the locale of the session, null to use unlocalized session
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
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

    /**
     * Execute the action specified by the given callback object within a new user Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param user      the user to open the session with
     * @param workspace the workspace name to log into
     * @param locale    the locale of the session, null to use unlocalized session
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
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

    public JCRStoreProvider getProvider(String path) {
        return sessionFactory.getProvider(path);
    }
}
