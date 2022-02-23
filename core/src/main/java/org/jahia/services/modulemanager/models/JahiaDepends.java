/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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


    public JahiaDepends(String dependency) {
        this.parsedString = dependency;
        String[] deps = dependency.split("=");
        this.moduleName = StringUtils.isNotBlank(deps[0]) ? deps[0].trim() : "";

        if (deps.length > 1 && StringUtils.isNotBlank(deps[1])) {
            range = VersionRange.valueOf(deps[1]);
        }
    }

    public boolean hasVersion() {
        return StringUtils.isNotEmpty(getMinVersion())
                || StringUtils.isNotEmpty(getMaxVersion());
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
