package org.jahia.bundles.securityfilter.core.grant;

import java.util.Map;

public interface Grant {

    boolean matches(Map<String, Object> query);
}
