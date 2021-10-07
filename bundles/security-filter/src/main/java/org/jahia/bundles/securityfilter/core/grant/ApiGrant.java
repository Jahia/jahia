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
