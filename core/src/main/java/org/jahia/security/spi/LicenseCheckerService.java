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
package org.jahia.security.spi;

import java.util.Optional;

/**
 * License checker interface.
 *
 * @author Sergiy Shyrkov
 */
public interface LicenseCheckerService {

    /**
     * Checks if the corresponding product feature is allowed by the current license.
     *
     * @param featureId the feature ID to be checked
     * @return <code>true</code> if the specified product feature is allowed by the license; <code>false</code> otherwise.
     */
    boolean checkFeature(String featureId);

    /**
     * This method check if the desired limit has been exceeded.
     *
     * @param featureId     ID of the feature where to find the limit (e.g: org.jahia.core)
     * @param limitName     Name of the limit to check (e.g: sites)
     * @return <code>true</code> if the desired limit has been exceeded, <code>false</code> if there is no limit or if not exceeded
     */
    boolean isLimitReached(String featureId, String limitName);

    /**
     * Checks whether or not the limit (if any) of logged in users has been reached.
     *
     * @return {@code true} if the limit has been reached, {@code false} otherwise
     */
    boolean isLoggedInUsersLimitReached();

    /**
     * This method get the site limit value.
     *
     * @return <code>Optional</code> with the site limit value if not null
     */
    Optional<Long> getSiteLimit();
}
