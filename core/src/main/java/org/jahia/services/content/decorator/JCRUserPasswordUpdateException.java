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

import javax.jcr.RepositoryException;

/**
 * This exception serves as a parent class for specific password update exceptions,
 * allowing callers to catch all password update issues with a single catch block
 * or handle specific cases individually.
 *
 * @author Jahia Solutions Group SA
 * @since 8.2.3.0
 */
public class JCRUserPasswordUpdateException extends RepositoryException {

    private static final long serialVersionUID = 1L;

    /**
     * The username for which the password update operation failed.
     */
    private final String username;

    /**
     * Constructs a new password update exception with the specified username and detail message.
     *
     * @param username the username for which the password update operation failed
     * @param message the detail message explaining the cause of the exception
     */
    public JCRUserPasswordUpdateException(String username, String message) {
        super(message);
        this.username = username;
    }

    /**
     * Returns the username for which the password update operation failed.
     *
     * @return the username, or null if not available
     */
    public String getUsername() {
        return username;
    }
}
