package org.jahia.bundles.securityfilter.core;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.modulemanager.util.PropertiesValues;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParserHelper {
    private ParserHelper() {
    }

    public static Set<String> buildSet(PropertiesValues nodeValues, String prop) {
        if (nodeValues.getProperty(prop) == null) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(Arrays.asList(StringUtils.split(nodeValues.getProperty(prop), ", ")));
    }
}
