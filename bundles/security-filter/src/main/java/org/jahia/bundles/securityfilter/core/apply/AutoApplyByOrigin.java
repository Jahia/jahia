package org.jahia.bundles.securityfilter.core.apply;

import org.jahia.services.modulemanager.util.PropertiesValues;
import javax.servlet.http.HttpServletRequest;

import static org.jahia.bundles.securityfilter.core.ParserHelper.isSameOrigin;

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
        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin == null) {
            requestOrigin = request.getHeader("Referer");
        }
        if (requestOrigin != null) {
            String originToCheck = (origin.equals("hosted") || origin.equals("same")) ? request.getRequestURL().toString() : origin;
            return isSameOrigin(originToCheck, requestOrigin);
        }

        return false;
    }
}
