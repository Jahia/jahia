/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.security.license;

import org.jahia.bin.Jahia;
import org.jahia.services.SpringContextSingleton;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * License checker interface.
 *
 * @author Sergiy Shyrkov
 */
public interface LicenseCheckerService {

    class Stub {

        private static volatile boolean initialized = false;
        private static volatile LicenseCheckerService instance;

        /**
         * @see LicenseCheckerService#checkFeature(String)
         */
        public static boolean isAllowed(String featureId) {
            LicenseCheckerService service = getInstance();
            return service != null && service.checkFeature(featureId);
        }

        private static LicenseCheckerService getInstance() {
            if (!initialized) {
                synchronized (Stub.class) {
                    if (!initialized) {
                        if (Jahia.isEnterpriseEdition()) {
                            if (SpringContextSingleton.getInstance().isInitialized()) {
                                try {
                                    instance = (LicenseCheckerService) SpringContextSingleton.getInstance().getContext().getBean("licenseChecker");
                                } catch (NoSuchBeanDefinitionException e) {
                                    // no bean defined
                                }
                            }
                        }
                        initialized = true;
                    }
                }
            }
            return instance;
        }
    }

    /**
     * Checks if the corresponding product feature is allowed by the current license.
     *
     * @param featureId the feature ID to be checked
     * @return <code>true</code> if the specified product feature is allowed by the license; <code>false</code> otherwise.
     */
    boolean checkFeature(String featureId);
}
