/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.security.license;

import org.jahia.utils.JahiaTools;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide a checker for action in the license file.
 * User: Serge Huber
 * Date: 29 juin 2006
 * Time: 17:14:12
 * Copyright (C) Jahia Inc.
 */
public class LicenseActionChecker {
    static Map checks = new HashMap();

    static public boolean isAuthorizedByLicense(String objectName, int siteID) {
        if (checks.containsKey(objectName+siteID)) {
            return ((Boolean) checks.get(objectName+siteID)).booleanValue();
        }
        String licenseObjectName = objectName;
        if (siteID > 0) {
            int siteIDPos = objectName.indexOf(Integer.toString(siteID));
            if (siteIDPos > -1) {
                licenseObjectName = JahiaTools.replacePattern(objectName, Integer.toString(siteID), "*");
            }
        }
        boolean licenseAuthorized = true;
        LicenseManager lm = LicenseManager.getInstance();
        License license = lm.getLicenseByComponentName(licenseObjectName);
        if (license != null) {
            licenseAuthorized = license.checkLimits();
        }
        checks.put(objectName+siteID, Boolean.valueOf(licenseAuthorized));
        return licenseAuthorized;
    }

}
