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
package org.jahia.settings.readonlymode;

import java.util.Objects;

import static org.jahia.settings.readonlymode.ReadOnlyModeController.*;

/**
 * Holds the read-only mode status of a specific origin
 *
 * @author cmoitrier
 * @since 7.3.2.1
 */
public final class ReadOnlyModeStatusInfo {

    private final String origin;
    private final ReadOnlyModeStatus value;

    public ReadOnlyModeStatusInfo(String origin, ReadOnlyModeStatus value) {
        this.origin = Objects.requireNonNull(origin, "origin");
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getOrigin() {
        return origin;
    }

    public ReadOnlyModeStatus getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ReadOnlyModeStatusInfo{origin='" + origin + '\'' + ", value=" + value + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadOnlyModeStatusInfo that = (ReadOnlyModeStatusInfo) o;
        return Objects.equals(origin, that.origin) && value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, value);
    }

}
