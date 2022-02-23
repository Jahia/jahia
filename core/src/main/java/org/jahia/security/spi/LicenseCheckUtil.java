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
package org.jahia.security.spi;

import org.jahia.osgi.BundleUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class LicenseCheckUtil {

    private static volatile boolean initialized = false;
    private static AtomicReference<LicenseCheckerService> instance = new AtomicReference<>();

    private LicenseCheckUtil() {
    }

    /**
     * @see LicenseCheckerService#checkFeature(String)
     */
    public static boolean isAllowed(String featureId) {
        LicenseCheckerService service = getInstance();
        return service != null && service.checkFeature(featureId);
    }

    /**
     * @see LicenseCheckerService#isLimitReached(String, String)
     */
    public static boolean isLimitReached(String componentName, String limitName) {
        LicenseCheckerService service = getInstance();
        return service != null && service.isLimitReached(componentName, limitName);
    }

    /**
     * @see LicenseCheckerService#isLoggedInUsersLimitReached()
     */
    public static boolean isLoggedInUsersLimitReached() {
        LicenseCheckerService service = getInstance();
        return (service != null) && service.isLoggedInUsersLimitReached();
    }

    /**
     * @see LicenseCheckerService#getSiteLimit()
     */
    public static Optional<Long> getSiteLimit() {
        LicenseCheckerService service = getInstance();
        if (service != null) {
            return service.getSiteLimit();
        }
        return Optional.empty();
    }


    private static LicenseCheckerService getInstance() {
        if (instance.get() == null && !initialized) {
            instance.compareAndSet(null, BundleUtils.getOsgiService(LicenseCheckerService.class, null));
            initialized = true;
        }
        return instance.get();
    }
}
