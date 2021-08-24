package org.jahia.services.securityfilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ScopeDefinition {

    String getScopeName();

    String getDescription();

    Map<String, String> getMetadata();

    boolean isValid(HttpServletRequest request);
}
