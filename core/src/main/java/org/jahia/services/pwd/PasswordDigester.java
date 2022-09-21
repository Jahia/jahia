/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.pwd;

/**
 * Common interface for all password encryption (hashing) operations.
 * 
 * @author Sergiy Shyrkov
 */
public interface PasswordDigester {

    /**
     * Create a digest of the provided password.
     * 
     * @param password
     *            the clear text password to be hashed
     * @return the digest of the provided password
     */
    String digest(String password);

    /**
     * Returns unique identifier of this digester to be able to distinguish between various hashing algorithms.
     * 
     * @return unique identifier of this digester to be able to distinguish between various hashing algorithms
     */
    String getId();

    /**
     * Should this digester become the default password digester, which is used in the system?
     * 
     * @return <code>true</code> if this digester becomes the default one, which is used in the system
     */
    boolean isDefault();

    /**
     * Checks, if the provided clear text password matches the specified digest, considering all aspects like salt, hashing iterations, etc.
     * 
     * @param password
     *            the clear text password to be checked
     * @param digest
     *            the digest against which the password will be matched
     * @return <code>true</code>, if the provided password matches its hashed equivalent
     */
    boolean matches(String password, String digest);
}
