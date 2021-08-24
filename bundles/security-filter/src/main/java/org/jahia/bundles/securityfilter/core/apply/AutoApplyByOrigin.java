package org.jahia.bundles.securityfilter.core.apply;

import org.jahia.services.modulemanager.util.PropertiesValues;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class AutoApplyByOrigin implements AutoApply {

    private String origin;

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
            try {
                String host = new URL(requestOrigin).getHost();
                String originToCheck = (origin.equals("hosted") || origin.equals("same")) ? request.getServerName() : origin;
                if (host.equals(originToCheck)) {
                    return true;
                }
            } catch (MalformedURLException e) {
                // Cannot parse URL
            }
        }
        return false;
    }
}
