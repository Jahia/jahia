/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import org.jahia.utils.comparator.VersionComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents version of a module.
 */
public class ModuleVersion implements Comparable<ModuleVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("[^0-9]+");

    private boolean isSnapshot;

    private List<Integer> orderedVersionNumbers = new ArrayList<Integer>();

    private String versionString;

    /**
     * Initializes an instance of this class.
     * @param versionString the plain text representation of the module version
     */
    public ModuleVersion(String versionString) {
        this.versionString = versionString;
        if (versionString.endsWith("SNAPSHOT")) {
            isSnapshot = true;
        }
        String[] numbers = VERSION_PATTERN.split(versionString);
        for (String number : numbers) {
            orderedVersionNumbers.add(Integer.parseInt(number));
        }
    }

    @Override
    public int compareTo(ModuleVersion o) {
        return VersionComparator.compare(this.versionString, o.versionString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleVersion that = (ModuleVersion) o;

        if (versionString != null ? !versionString.equals(that.versionString) : that.versionString != null) {
            return false;
        }

        return true;
    }

    /**
     * Returns a list of ordered version numbers.
     * @return a list of ordered version numbers
     */
    public List<Integer> getOrderedVersionNumbers() {
        return orderedVersionNumbers;
    }

    @Override
    public int hashCode() {
        return versionString != null ? versionString.hashCode() : 0;
    }

    /**
     * Checks if the current version is a SNAPSHOT or not.
     * @return <code>true</code> if the current version is a SNAPSHOT; <code>false</code> otherwise
     */
    public boolean isSnapshot() {
        return isSnapshot;
    }

    @Override
    public String toString() {
        return versionString;
    }
}
