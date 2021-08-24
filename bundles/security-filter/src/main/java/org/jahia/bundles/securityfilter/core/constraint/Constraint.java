package org.jahia.bundles.securityfilter.core.constraint;

import javax.servlet.http.HttpServletRequest;

public interface Constraint {
    boolean isValid(HttpServletRequest request);
}
