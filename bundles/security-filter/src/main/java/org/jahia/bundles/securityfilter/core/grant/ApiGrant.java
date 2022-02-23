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
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
}
