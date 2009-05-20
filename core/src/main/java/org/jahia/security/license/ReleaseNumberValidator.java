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

import org.jahia.bin.Jahia;
import org.jahia.resourcebundle.ResourceMessage;
import org.jahia.utils.Version;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ReleaseNumberValidator extends VersionValidator {

    public ReleaseNumberValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {
        internalVersion = new Version(Double.toString(Jahia.getReleaseNumber()));
        if (super.assertEquals(value)) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.ReleaseNumberValidator.invalidReleaseNumber.label", internalVersion, value);
            return false;
        }
    }

    public boolean assertInRange(String fromValue, String toValue) {
        internalVersion = new Version(Double.toString(Jahia.getReleaseNumber()));
        if (super.assertInRange(fromValue, toValue)) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.ReleaseNumberValidator.releaseNumberNotInRange.label", internalVersion, fromValue, toValue);
            return false;
        }
    }

}