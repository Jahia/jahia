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
import java.util.stream.Collectors;

/**
 * Check pattern on the requested API
 */
public class ApiGrant implements Grant {
    /**
     * The top-level configuration key for this grant type.
     */
    public static final String KEY = "api";

    /**
     * Set of recognized keys for the {@code api} configuration block.
     */
    public static final Set<String> KNOWN_KEYS = Set.of("include", "exclude");

    /**
     * {@link GrantBuilder} for {@link ApiGrant}.
     * Kept here so that {@code ApiGrant} remains the single owner of its parsing logic.
     */
    public static final GrantBuilder BUILDER = new GrantBuilder() {
        @Override
        public String getKey() {
            return KEY;
        }

        @Override
        public Grant build(PropertiesValues grantValues) throws IllegalArgumentException {
            return ApiGrant.build(grantValues);
        }
    };

    private final Set<String> apis;
    private final Set<String> excludes;

    public ApiGrant(Set<String> apis, Set<String> excludes) {
        this.apis = apis;
        this.excludes = excludes;
    }

    /**
     * Builds an {@link ApiGrant} from the given grant configuration values.
     *
     * <p>Supports two forms for the {@code api} key:
     * <ul>
     *   <li><b>Scalar</b> — {@code api: "graphql"}: matches the listed API name(s) with no exclusions.</li>
     *   <li><b>Block</b> — {@code api: {include: [...], exclude: [...]}}: matches APIs listed under
     *       {@code include} while excluding those listed under {@code exclude}.
     *       Recognized keys are defined in {@link #KNOWN_KEYS}.</li>
     * </ul>
     *
     * @param grantValues the parsed configuration values for a single grant entry
     * @return an {@link ApiGrant} if an {@code api} key is present, or {@code null} if this grant
     * entry does not contain an {@code api} definition
     * @throws IllegalArgumentException if the {@code api} block contains keys not in {@link #KNOWN_KEYS}
     */
    public static Grant build(PropertiesValues grantValues) throws IllegalArgumentException {
        PropertiesValues subValues = grantValues.getValues(KEY);

        if (!subValues.getKeys().isEmpty()) {
            Set<String> unknownKeys = subValues.getKeys().stream().filter(k -> !KNOWN_KEYS.contains(k)).collect(Collectors.toSet());
            if (!unknownKeys.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid key(s) in '" + KEY + "' block: " + unknownKeys + ". Valid keys are: " + KNOWN_KEYS);
            }
            return new ApiGrant(ParserHelper.buildSet(subValues, "include"), ParserHelper.buildSet(subValues, "exclude"));
        } else if (grantValues.getKeys().contains(KEY)) {
            return new ApiGrant(ParserHelper.buildSet(grantValues, KEY), Collections.emptySet());
        }

        return null;
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
