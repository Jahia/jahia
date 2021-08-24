package org.jahia.bundles.securityfilter.core.apply;

import javax.servlet.http.HttpServletRequest;

public interface AutoApply {
    boolean shouldApply(HttpServletRequest request);
}
