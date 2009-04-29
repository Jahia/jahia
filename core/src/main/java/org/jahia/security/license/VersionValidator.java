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
/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jahia.security.license;

import org.jahia.utils.Version;
import org.jahia.resourcebundle.ResourceMessage;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VersionValidator extends AbstractValidator {

    protected Version internalVersion = new Version("1.0");

    public VersionValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {
        Version licenseVersion = new Version(value);
        if (licenseVersion.equals(internalVersion)) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.VersionValidator.invalidVersion.label", internalVersion, value);
            return false;
        }
    }

    public boolean assertInRange(String fromValue, String toValue) {
        Version fromVersion = new Version(fromValue);
        Version toVersion = new Version(toValue);
        if ((fromVersion.compareTo(internalVersion) <= 0) &&
            (toVersion.compareTo(internalVersion) > 0)) {
            return true;
        }
        errorMessage = new ResourceMessage("org.jahia.security.license.VersionValidator.versionNotInRange.label", internalVersion, fromValue, toValue);
        return false;
    }

}
