package org.jahia.bundles.securityfilter.core.apply;

import com.ctc.wstx.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.modulemanager.util.PropertiesValues;
import javax.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.jahia.bundles.securityfilter.core.ParserHelper.isSameOrigin;
import static org.kie.internal.task.api.model.AccessType.Url;

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
        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin == null) {
            // Get the referer in case origin is not available
            requestOrigin = request.getHeader("Referer");
            try {
                // Keep only the scheme / servername / port of the url from the referer
                requestOrigin = StringUtils.substringBefore(requestOrigin, new URL(requestOrigin).getPath());
            } catch (MalformedURLException e) {
                // Unable to parse the Referrer
               return false;
            }
        }
        // In case of hosted or same, compare to the request
        if (origin.equals("hosted") || origin.equals("same")) {
            return isSameOrigin(request, requestOrigin);
        } else {
            return isSameOrigin(origin, requestOrigin);
        }
    }
}
