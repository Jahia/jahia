package org.jahia.api.content;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
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
public interface JCRTemplate {
    /**
     * @return Returns the sessionFactory.
     */
    JCRSessionFactory getSessionFactory();

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
    <X> X doExecuteWithSystemSessionInSameWorkspaceAndLocale(JCRCallback<X> callback) throws RepositoryException;

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
    <X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException;

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
    <X> X doExecuteWithSystemSessionAsUser(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;

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
    <X> X doExecute(String username, String realm, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;

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
    <X> X doExecute(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;
}
