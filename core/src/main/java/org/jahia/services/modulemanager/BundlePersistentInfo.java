/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager;

import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.BundleStartLevel;

/**
 * Represents persistent state of a bundle
 */
public final class BundlePersistentInfo {

    private final String location;
    private final int state;
    private final String symbolicName;
    private final String version;
    private final int startLevel;

    public BundlePersistentInfo(Bundle bundle) {
        this(
                bundle.getLocation(),
                bundle.getSymbolicName(),
                BundleUtils.getModuleVersion(bundle),
                BundleUtils.getPersistentState(bundle),
                bundle.adapt(BundleStartLevel.class).getStartLevel()
        );
    }

    public BundlePersistentInfo(String location, String symbolicName, String version, int state, int startLevel) {
        this.location = location;
        this.symbolicName = symbolicName;
        this.version = version;
        this.state = state;
        this.startLevel = startLevel;
    }

    public String getLocation() {
        return location;
    }

    public int getState() {
        return state;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getVersion() {
        return version;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public String getLocationProtocol() {
        int delimiter = (location == null) ? -1 : location.indexOf(':');
        return (delimiter > -1) ? location.substring(0, delimiter) : null;
    }

    /**
     * Returns {@code true} if this instance does refer to the
     * same bundle version as {@code other}.
     *
     * @param other the instance to check against
     * @return {@code true} if {@symbolicName} and {@version} of
     *  both instances are the same, {@code false} otherwise
     */
    public boolean isSameVersionAs(BundlePersistentInfo other) {
        return other.getSymbolicName().equals(symbolicName)
                && other.getVersion().equals(version);
    }

}
