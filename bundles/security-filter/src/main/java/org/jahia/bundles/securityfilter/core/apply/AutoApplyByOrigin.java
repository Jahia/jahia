package org.jahia.bundles.securityfilter.core.apply;

import org.jahia.services.modulemanager.util.PropertiesValues;

import javax.servlet.http.HttpServletRequest;

import static org.jahia.bundles.securityfilter.core.ParserHelper.isSameOriginFromHeader;

public class AutoApplyByOrigin implements AutoApply {

    private final String origin;

    public static AutoApply build(PropertiesValues values) {
        String origin = values.getProperty("origin");
        if (origin != null) {
            return new AutoApplyByOrigin(origin);
        }

        return null;
    }

    public AutoApplyByOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public boolean shouldApply(HttpServletRequest request) {
        // Get origin from the request
        return isSameOriginFromHeader(request, origin);
    }
}
