package org.jahia.bundles.securityfilter.core.grant;

import org.jahia.bundles.securityfilter.core.ParserHelper;
import org.jahia.services.modulemanager.util.PropertiesValues;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
        boolean includeMatch = apis.isEmpty() || (apiToCheck != null && apis.stream().anyMatch(api -> api.equals(apiToCheck) || apiToCheck.startsWith(api + ".")));
        boolean excludeMatch = excludes.isEmpty() || apiToCheck == null || excludes.stream().noneMatch(api -> api.equals(apiToCheck) || apiToCheck.startsWith(api + "."));
        return includeMatch && excludeMatch;
    }
}
