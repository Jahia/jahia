/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.modulemanager.models;

import org.apache.commons.lang.StringUtils;

/**
 * Parser for jahia-depends value
 */
public class JahiaDepends {

    private String moduleName = "";
    private String minVersion = null;
    private String maxVersion = null;


    public JahiaDepends(String dependency) {
        String[] deps = dependency.split("=");
        this.moduleName = StringUtils.isNotBlank(deps[0]) ? deps[0].trim() : "";

        // parse version
        if (deps.length > 1 && StringUtils.isNotEmpty(deps[1])) {
            String[] version = deps[1].split(",");
            if (isMinVersion(version[0])) {
                String min = version[0].trim().substring(1);
                this.minVersion = (StringUtils.isNotBlank(min)) ? min.trim() : null;
            }
            if (isMaxVersion(version[1])) {
                String max = StringUtils.chop(version[1].trim());
                this.maxVersion = (StringUtils.isNotBlank(max)) ? max.trim() : null;
            }
        }
    }

    public boolean hasVersion() {
        return hasMaxVersion() || hasMinVersion();
    }

    public boolean hasMaxVersion() {
        return maxVersion != null;
    }

    public boolean hasMinVersion() {
        return minVersion != null;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public static boolean isMinVersion(String version) {
        return (StringUtils.isNotBlank(version)) && version.trim().startsWith("[");
    }

    public static boolean isMaxVersion(String version) {
        return (StringUtils.isNotBlank(version)) && version.trim().endsWith("]");
    }

}
