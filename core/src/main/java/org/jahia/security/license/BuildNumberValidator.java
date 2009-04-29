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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class BuildNumberValidator extends AbstractValidator {
    public BuildNumberValidator (String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals (String value) {
        int licenseBuildNumber = Integer.parseInt(value);
        if (licenseBuildNumber == Jahia.getBuildNumber()) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.BuildNumberValidator.invalidBuildNumber.label",  new Integer(Jahia.getBuildNumber()), new Integer(licenseBuildNumber));
            return false;
        }
    }

    public boolean assertInRange (String fromValue, String toValue) {
        int minBuildNumber = Integer.parseInt(fromValue);
        int maxBuildNumber = Integer.parseInt(toValue);
        if ( (Jahia.getBuildNumber() >= minBuildNumber) &&
             (Jahia.getBuildNumber() < maxBuildNumber) ) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.BuildNumberValidator.buildNumberNotInRange.label", new Integer(Jahia.getBuildNumber()), new Integer(minBuildNumber), new Integer(maxBuildNumber));
            return false;
        }
    }

}