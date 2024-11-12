/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
