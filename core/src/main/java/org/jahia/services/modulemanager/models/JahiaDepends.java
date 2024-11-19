/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.modulemanager.models;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

import static org.jahia.services.modulemanager.Constants.*;

/**
 * Parser for jahia-depends value
 */
public class JahiaDepends {

    private String moduleName = "";
    private VersionRange range = null;
    private String parsedString = null;

    private boolean isOptional = false;


    public JahiaDepends(String dependency) {
        this.parsedString = dependency;
        String[] deps = dependency.split("=");
        this.moduleName = StringUtils.isNotBlank(deps[0]) ? deps[0].trim() : "";

        if (deps.length > 1 && StringUtils.isNotBlank(deps[1])) {
            String rangeStr = deps[1];
            rangeStr = rangeStr.replace(";optional", "");
            rangeStr = rangeStr.replace("optional", "");
            this.isOptional = !rangeStr.equals(deps[1]); // optional keyword existed and was removed
            if (!rangeStr.isEmpty()) {
                range = VersionRange.valueOf(rangeStr);
            }
        }
    }

    public boolean hasVersion() {
        return StringUtils.isNotEmpty(getMinVersion())
                || StringUtils.isNotEmpty(getMaxVersion());
    }

    public boolean isOptional() {
        return this.isOptional;
    }

    /** @return true if this jahia-depends entry has additional attributes specified */
    public boolean hasAttributes() {
        return this.hasVersion() || this.isOptional();
    }


    public String getModuleName() {
        return moduleName;
    }

    public String getMinVersion() {
        return (range != null && range.getLeft() != null) ? range.getLeft().toString() : "";
    }

    public String getMaxVersion() {
        return (range != null && range.getRight() != null) ? range.getRight().toString() : "";
    }

    public VersionRange getVersionRange() {
        return range;
    }

    public boolean inRange(String version) {
        Version v = new Version(toOsgiVersion(version));
        return (range == null) || range.includes(v);
    }

    public String toFilterString() {
        String verFilter = (range != null) ? range.toFilterString(OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY) : "";
        String filter = String.format("(%s=%s)", OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY, moduleName);
        if (!verFilter.isEmpty()) {
            filter = verFilter.startsWith("(&") ?
                    verFilter.replace("&", "&" + filter) :
                    String.format("(&%s%s)", filter, verFilter);
        }
        return filter;
    }

    public static JahiaDepends parse(String dependency) {
        return new JahiaDepends(dependency);
    }

    /** Workaround to convert maven project version to OSGI-compatible version */
    public static String toOsgiVersion(String version) {
        return org.apache.felix.utils.version.VersionCleaner.clean(version);
    }

    /** @return if clause starts with VersionRange.LEFT_OPEN or VersionRange.LEFT_CLOSED */
    public static boolean isOpenClause(String clause) {
        return StringUtils.isNotBlank(clause) && (
                clause.trim().startsWith(String.valueOf(VersionRange.LEFT_OPEN)) ||
                clause.trim().startsWith(String.valueOf(VersionRange.LEFT_CLOSED)) );
    }

    @Override public String toString() {
        return parsedString;
    }
}
