package org.jahia.bundles.securityfilter.core.apply;

import org.jahia.services.modulemanager.util.PropertiesValues;

import javax.servlet.http.HttpServletRequest;

public class AlwaysAutoApply implements AutoApply {

    public static AutoApply build(PropertiesValues values) {
        String value = values.getProperty("always");
        if (Boolean.parseBoolean(value)) {
            return new AlwaysAutoApply();
        }

        return null;
    }

    public AlwaysAutoApply() {
    }

    @Override
    public boolean shouldApply(HttpServletRequest request) {
        return true;
    }
}
