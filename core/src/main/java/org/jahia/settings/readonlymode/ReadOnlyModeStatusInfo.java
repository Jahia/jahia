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
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
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
