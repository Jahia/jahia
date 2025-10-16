/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.interceptor;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.decorator.JCRUserPasswordUpdateAuthorizationException;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Collections;

/**
 * Interceptor that controls password property modifications on user nodes.
 * When isUserPasswordUpdateRequiringPreviousPassword is enabled, it blocks
 * direct password modifications and only allows them through the secured
 * setPassword(currentPassword, newPassword) method using a thread-local flag.
 *
 * Enhanced security features:
 * - User-specific authorization (prevents cross-user authorization abuse)
 * - Time-limited authorization (X-second validity window, configured via jahia.properties)
 * - Automatic cleanup to prevent thread-local leakage
 *
 * Note: This interceptor will not reject password sets on newly created user nodes,
 * as initial password assignment during user creation is considered a legitimate
 * administrative operation.
 *
 * @author Jahia Solutions Group SA
 */
public class UserPasswordUpdateInterceptor extends BaseInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserPasswordUpdateInterceptor.class);

    // Enhanced thread-local to store authorization context
    private static final ThreadLocal<PasswordUpdateContext> AUTHORIZED_CONTEXT = new ThreadLocal<>();

    /**
     * Context class that stores user-specific authorization with time-limited validity.
     * This prevents authorization leakage between different users and provides
     * automatic expiration of authorization grants.
     */
    public static class PasswordUpdateContext {
        private final String username;
        private final long timestamp;
        private final long expirationTime;

        public PasswordUpdateContext(String username, long validityDurationMs) {
            this.username = username;
            this.timestamp = System.currentTimeMillis();
            this.expirationTime = timestamp + validityDurationMs;
        }

        /**
         * Checks if this authorization context is valid for the specified username and has not expired.
         */
        public boolean isValidFor(String username) {
            return this.username.equals(username) && !isExpired();
        }

        public String getUsername() {
            return username;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }
    }

    public UserPasswordUpdateInterceptor() {
        // Configure interceptor to only apply to j:password property on jnt:user nodes
        setPropertyNames(Collections.singleton(JCRUserNode.J_PASSWORD));
        setNodeTypes(Collections.singleton(Constants.JAHIANT_USER));
    }

    /**
     * Authorizes password update for a specific user with time-limited validity.
     *
     * @param username the username for which password update is authorized
     */
    public static void authorizePasswordUpdate(String username) {
        long timeoutMs = SettingsBean.getInstance().getUserPasswordUpdateAuthorizationTimeoutMs();
        AUTHORIZED_CONTEXT.set(new PasswordUpdateContext(username, timeoutMs));
        if (logger.isDebugEnabled()) {
            logger.debug("Password update authorized for user: {} in current thread for {} ms", username, timeoutMs);
        }
    }

    /**
     * Clears the authorization flag after password property modification.
     * This ensures the flag doesn't leak to other operations in the same thread.
     */
    public static void clearPasswordUpdateAuthorization() {
        PasswordUpdateContext context = AUTHORIZED_CONTEXT.get();
        AUTHORIZED_CONTEXT.remove();
        if (logger.isDebugEnabled() && context != null) {
            logger.debug("Password update authorization cleared for user: {} in current thread", context.getUsername());
        }
    }

    /**
     * Checks if password update is currently authorized for the specified username.
     * Package-private for testing purposes.
     *
     * @param username the username to check authorization for
     * @return true if authorized and not expired, false otherwise
     */
    static boolean isPasswordUpdateAuthorized(String username) {
        PasswordUpdateContext context = AUTHORIZED_CONTEXT.get();
        if (context == null) {
            return false;
        }

        // Clean up expired contexts automatically
        if (context.isExpired()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing expired password update authorization for user: {}", context.getUsername());
            }
            AUTHORIZED_CONTEXT.remove();
            return false;
        }

        return context.isValidFor(username);
    }

    /**
     * Performs the authorization check for password updates.
     * Throws JCRUserPasswordUpdateAuthorizationException if the update is not authorized.
     *
     * @param node the node being modified
     * @throws JCRUserPasswordUpdateAuthorizationException if password update is not authorized
     */
    private void checkPasswordUpdateAuthorization(JCRNodeWrapper node) throws JCRUserPasswordUpdateAuthorizationException {
        // Only intercept if :
        // - the SettingsBean requires previous password for updates
        // - the node is not new (new users can have password set freely)
        if (node.isNew() || !SettingsBean.getInstance().isUserPasswordUpdateRequiringPreviousPassword()) {
            return;
        }

        // Check if the current session has the 'setUsersPassword' permission
        try {
            // force the usage of a NON system session by always using real current user session.
           if (JCRSessionFactory.getInstance().getCurrentUserSession().getRootNode().hasPermission("setUsersPassword")) {
               return;
           }
        } catch (RepositoryException e) {
            // ignore and proceed to thread-local check
            logger.warn("Error checking 'setUsersPassword' permission during password update on user: {}, (More details in debug)", node.getPath());
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }

        // If not authorized via thread-local flag, block the modification
        if (!isPasswordUpdateAuthorized(node.getName())) {
            if (logger.isDebugEnabled()) {
                PasswordUpdateContext context = AUTHORIZED_CONTEXT.get();
                if (logger.isDebugEnabled()) {
                    if (context == null) {
                        logger.debug("Password update blocked for user: {} - no authorization context", node.getName());
                    } else if (context.isExpired()) {
                        logger.debug("Password update blocked for user: {} - authorization expired", node.getName());
                    } else {
                        logger.debug("Password update blocked for user: {} - authorized for different user: {}", node.getName(), context.getUsername());
                    }
                }
            }
            throw new JCRUserPasswordUpdateAuthorizationException(node.getName());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Authorized password update proceeding for user node: {}", node.getPath());
        }
    }

    @Override
    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws RepositoryException {
        checkPasswordUpdateAuthorization(node);
        return super.beforeSetValue(node, name, definition, originalValue);
    }

    @Override
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws RepositoryException {
        checkPasswordUpdateAuthorization(node);
        return super.beforeSetValues(node, name, definition, originalValues);
    }

    @Override
    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws RepositoryException {
        checkPasswordUpdateAuthorization(node);
        super.beforeRemove(node, name, definition);
    }
}
