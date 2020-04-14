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
