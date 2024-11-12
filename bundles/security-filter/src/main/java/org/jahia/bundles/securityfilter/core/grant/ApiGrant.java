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
package org.jahia.bundles.securityfilter.core.grant;

import org.jahia.bundles.securityfilter.core.ParserHelper;
import org.jahia.services.modulemanager.util.PropertiesValues;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Check pattern on the requested API
 */
public class ApiGrant implements Grant {
    private Set<String> apis;
    private Set<String> excludes;

    public static Grant build(PropertiesValues grantValues) {
        PropertiesValues subValues = grantValues.getValues("api");

        if (!subValues.getKeys().isEmpty()) {
            return new ApiGrant(ParserHelper.buildSet(subValues, "include"), ParserHelper.buildSet(subValues, "exclude"));
        } else if (grantValues.getKeys().contains("api")) {
            return new ApiGrant(ParserHelper.buildSet(grantValues, "api"),Collections.emptySet());
        }

        return null;
    }

    public ApiGrant(Set<String> apis, Set<String> excludes) {
        this.apis = apis;
        this.excludes = excludes;
    }

    @Override
    public boolean matches(Map<String, Object> query) {
        String apiToCheck = (String) query.get("api");
        Predicate<String> predicate = api -> apiToCheck != null && (api.equals(apiToCheck) || apiToCheck.startsWith(api + "."));
        return Grant.anyMatch(apis, predicate) && Grant.noneMatch(excludes, predicate);
    }

    @Override
    public String toString() {
        return String.format("apis: [%s], excludes: [%s]", String.join(",", apis), String.join(",", excludes));
    }
}
