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

/**
 * A listener to implement in order to be notified when full read-only mode is turned on or off.
 *
 * @author cmoitrier
 * @since 7.3.2.1
 * @see ReadOnlyModeController
 */
public interface ReadOnlyModeSwitchListener {

    /**
     * Callback method invoked when read-only mode has been turned on or off.
     *
     * @param enabled {@code true} if read-only mode has been turned on, {@code false} otherwise
     */
    void onReadOnlyModeSwitched(boolean enabled);

}
