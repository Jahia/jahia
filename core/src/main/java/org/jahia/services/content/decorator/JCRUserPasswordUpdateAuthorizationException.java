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
package org.jahia.services.content.decorator;

/**
 * Exception thrown when a direct password property modification is attempted without proper authorization.
 * This exception is thrown by {@link org.jahia.services.content.interceptor.UserPasswordUpdateInterceptor}
 * when the password update security feature is enabled and a direct property modification is attempted
 * without proper authorization.
 *
 * <p>Password updates can be authorized in several ways:</p>
 * <ul>
 *   <li>Using the secured {@link JCRUserNode#setPassword(String, String)} method (recommended)</li>
 *   <li>Programmatically setting the authorization flag via
 *       {@link org.jahia.services.content.interceptor.UserPasswordUpdateInterceptor#authorizePasswordUpdate()}</li>
 *   <li>During new user creation (authorization is automatically bypassed for new nodes)</li>
 *   <li>When the security feature is disabled via
 *       {@link org.jahia.settings.SettingsBean#isUserPasswordUpdateRequiringPreviousPassword()}</li>
 * </ul>
 *
 * @author Jahia Solutions Group SA
 * @since 8.2.3.0
 * @see JCRUserNode#setPassword(String, String)
 * @see org.jahia.services.content.interceptor.UserPasswordUpdateInterceptor
 * @see org.jahia.services.content.interceptor.UserPasswordUpdateInterceptor#authorizePasswordUpdate()
 * @see org.jahia.services.content.interceptor.UserPasswordUpdateInterceptor#clearPasswordUpdateAuthorization()
 * @see org.jahia.settings.SettingsBean#isUserPasswordUpdateRequiringPreviousPassword()
 */
public class JCRUserPasswordUpdateAuthorizationException extends JCRUserPasswordUpdateException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new authorization exception with a default message.
     *
     * @param username the username for which the unauthorized password update was attempted
     */
    public JCRUserPasswordUpdateAuthorizationException(String username) {
        super(username, "Direct password modification is not allowed for user: " + username +
              ". Use the secured setPassword(currentPassword, newPassword) method.");
    }
}
