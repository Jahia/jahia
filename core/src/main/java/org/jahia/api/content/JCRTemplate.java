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
package org.jahia.api.content;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
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
public interface JCRTemplate {
    /**
     * @return Returns the sessionFactory.
     */
    JCRSessionFactory getSessionFactory();

    /**
     * Execute the action specified by the given callback object within a system Session.<br/>
     * <br/>
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
     * Execute the action specified by the given callback object within a system Session.<br/>
     * <br/>
     * The workspace logged into will be the repository's default workspace. The user
     * will be the current user of the thread obtained by JcrSessionFilter.getCurrentUser().<br/>
     * The locale will be "default".
     *
     * @param callback the <code>JCRCallback</code> that executes the client
     *                 operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    <X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException;

    /**
     * Execute the action specified by the given callback object within a system Session optimized for operations
     * on many nodes (Session cache will be disabled to optimize memory footprint).<br/>
     * <br/>
     * The workspace logged into will be the repository's default workspace. The user
     * will be the current user of the thread obtained by JcrSessionFilter.getCurrentUser().<br/>
     * The locale will be "default".<br/>
     * <br/>
     * You need to takes into consideration that a 'long' session must not exceed some thresholds anyway, like about 100+ nodes created
     * or modified (write cache still in use). In such situation you may split/batch your work and call the JCRSessionWrapper.save()
     * method regularly to flush write caches.
     *
     * @param callback the <code>JCRCallback</code> that executes the client operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    <X> X doExecuteWithLongSystemSession(JCRCallback<X> callback) throws RepositoryException;

    /**
     * Execute the action specified by the given callback object within a system Session.
     * <p/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.<br/>
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.<br/>
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
     * Execute the action specified by the given callback object within a system Session optimized for operations
     * on many nodes (Session cache will be disabled to optimize memory footprint).<br/>
     * <br/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.<br/>
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.<br/>
     * The locale will be "default".<br/>
     * <br/>
     * You need to takes into consideration that a 'long' session must not exceed some thresholds anyway, like about 100+ nodes created
     * or modified (write cache still in use). In such situation you may split/batch your work and call the JCRSessionWrapper.save()
     * method regularly to flush write caches.
     *
     * @param user           the user to open the session with
     * @param workspace      the workspace name to log into
     * @param locale         the locale of the session, null to use unlocalized session
     * @param callback       the <code>JCRCallback</code> that executes the client
     *                       operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    <X> X doExecuteWithLongSystemSessionAsUser(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;

    /**
     * Execute the action specified by the given callback object within a new user Session.<br/>
     * <br/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.<br/>
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.<br/>
     * The locale will be "default".
     *
     * @param username  the username to open the session with
     * @param workspace the workspace name to log into
     * @param locale    the locale of the session, null to use unlocalized session
     * @param callback  the <code>JCRCallback</code> that executes the client
     *                  operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    <X> X doExecute(String username, String realm, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;

    /**
     * Execute the action specified by the given callback object within a new user Session.<br/>
     * <br/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.<br/>
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.<br/>
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

    /**
     * Execute the action specified by the given callback object within a new user Session optimized for operations
     * on many nodes (Session cache will be disabled to optimize memory footprint).<br/>
     * <br/>
     * The workspace logged into will be the one given by the parameter or if null, the repository's
     * default workspace will be taken.<br/>
     * The user will be the one passed by the parameter or if null the current user of the thread
     * obtained by JcrSessionFilter.getCurrentUser() will be taken.<br/>
     * The locale will be "default".<br/>
     * <br/>
     * You need to takes into consideration that a 'long' session must not exceed some thresholds anyway, like about 100+ nodes created
     * or modified (write cache still in use). In such situation you may split/batch your work and call the JCRSessionWrapper.save()
     * method regularly to flush write caches.
    *
     * @param user      the user to open the session with
     * @param workspace the workspace name to log into
     * @param locale    the locale of the session, null to use unlocalized session
     * @param callback  the <code>JCRCallback</code> that executes the client operation
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    <X> X doExecuteWithLongSession(JahiaUser user, String workspace, Locale locale, JCRCallback<X> callback) throws RepositoryException;
}
