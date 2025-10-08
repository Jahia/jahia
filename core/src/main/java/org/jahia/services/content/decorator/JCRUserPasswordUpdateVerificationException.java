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
 * Exception thrown when user password verification fails during password update operations.
 * This typically occurs when an incorrect current password is provided to the
 * {@link JCRUserNode#setPassword(String, String)} method.
 *
 * <p>This exception indicates that the password update was rejected due to
 * authentication failure, not policy violations or technical errors.</p>
 *
 * @author Jahia Solutions Group SA
 * @since 8.2.3.0
 * @see JCRUserNode#setPassword(String, String)
 * @see JCRUserNode#verifyPassword(String)
 */
public class JCRUserPasswordUpdateVerificationException extends JCRUserPasswordUpdateException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new password verification exception with a default message.
     *
     * @param username the username for which password verification failed
     */
    public JCRUserPasswordUpdateVerificationException(String username) {
        super(username, "Password update failed due to wrong current password for user: " + username);
    }
}
