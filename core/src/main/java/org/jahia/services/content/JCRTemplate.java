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
 * @author C�dric Mailleux
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
        return doExecuteWithSystemSession(null, null, null, false, callback);
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
        return doExecuteWithSystemSession(username, null, null, false, callback);
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
        return doExecuteWithSystemSession(username, workspace, null, false, callback);
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
     * @param eventsDisabled
     * @param callback       the <code>JCRCallback</code> that executes the client
     *                       operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    public <X> X doExecuteWithSystemSession(String username, String workspace, Locale locale, boolean eventsDisabled, JCRCallback<X> callback) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getSystemSession(username, workspace, locale, eventsDisabled);
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
        JCRSessionWrapper session = null;
        if (useSystemSession) {
            return doExecuteWithSystemSession(username, workspace, locale, false, callback);
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
        JCRSessionWrapper session = null;
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