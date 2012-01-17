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

package org.jahia.services.content;

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
public class JCRTemplate {
    private JCRSessionFactory sessionFactory;
    private static JCRTemplate instance;

    private JCRTemplate() {
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
    public <X> X doExecuteWithSystemSessionInSameWorkspaceAndLocale(JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = getSessionFactory().getCurrentUserSession();
        if (session == null) {
            throw new RepositoryException("Trying to execute as a system session using current workspace and locale when no current user session exists !");
        }
        return doExecuteWithSystemSession(null, session.getWorkspace().getName(), session.getLocale(), callback);
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
    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(null, null, null, callback);
    }

    /**
     * Execute the action specified by the given callback object within a system Session.
     * <p/>
     * The workspace logged into will be the repository's default workspace. The user
     * will be the one passed by the parameter or if null the current user of the thread obtained
     * by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param username the user name to open the session with
     * @param callback the <code>JCRCallback</code> that executes the client
     *                 operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithSystemSession(String username, JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(username, null, null, callback);
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
     * @param username  the user name to open the session with
     * @param workspace the workspace name to log into
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithSystemSession(String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(username, workspace, null, callback);
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
     * @param username       the user name to open the session with
     * @param workspace      the workspace name to log into
     * @param locale         the locale of the session, null to use unlocalized session
     * @param callback       the <code>JCRCallback</code> that executes the client
     *                       operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithSystemSession(String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            if (username != null && username.startsWith(" system ")) {
                throw new IllegalArgumentException("the username cannot start by \" system \"");
            }
            session = sessionFactory.getSystemSession(username, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * Execute the action specified by the given callback object within a new unlocalized user Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param username  the user name to open the session with
     * @param workspace the workspace name to log into
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithUserSession(String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(username, workspace);
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
     * @param username  the user name to open the session with
     * @param workspace the workspace name to log into
     * @param locale    the locale of the session, null to use unlocalized session
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithUserSession(String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getUserSession(username, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    /**
     * Execute the action specified by the given callback object within a new system or user Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param useSystemSession If the session to be used is system
     * @param username         the user name to open the session with
     * @param workspace        the workspace name to log into
     * @param locale           the locale of the session, null to use unlocalized session
     * @param callback         the <code>JCRCallback</code> that executes the client
     *                         operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecute(boolean useSystemSession, String username, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException {
        if (useSystemSession) {
            return doExecuteWithSystemSession(username, workspace, locale, callback);
        } else {
            return doExecuteWithUserSession(username, workspace, locale, callback);
        }
    }

    /**
     * Execute the action specified by the given callback object within a new system or user Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.
     * The locale will be "default".
     *
     * @param useSystemSession If the session to be used is system
     * @param username         the user name to open the session with
     * @param workspace        the workspace name to log into
     * @param locale           the locale of the session, null to use unlocalized session
     * @param callback         the <code>JCRCallback</code> that executes the client
     *                         operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecute(boolean useSystemSession, String username, String workspace, JCRCallback<X> callback) throws RepositoryException {
        if (useSystemSession) {
            return doExecuteWithSystemSession(username, workspace, callback);
        } else {
            return doExecuteWithUserSession(username, workspace, callback);
        }
    }


    /**
     * Obtain the JCRTemplate singleton
     *
     * @return the JCRTemplate singleton instance
     */
    public static JCRTemplate getInstance() {
        if (instance == null) {
            instance = new JCRTemplate();
        }
        return instance;
    }

    public JCRStoreProvider getProvider(String path) {
        return sessionFactory.getProvider(path);
    }
}