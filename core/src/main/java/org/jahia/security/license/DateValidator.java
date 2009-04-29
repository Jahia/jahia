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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jahia.resourcebundle.ResourceMessage;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DateValidator extends AbstractValidator {

    SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
    Date nowDate = new Date();

    public DateValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {
        Date licenseDate;
        try {
            licenseDate = dateFormat.parse(value);
        } catch (ParseException e) {
            errorMessage = new ResourceMessage("org.jahia.security.license.DateValidator.invalidLicenseDateValue.label", value);
            return false;
        }
        if (licenseDate.after(nowDate)) {
            return true;
        } else {
            errorMessage = new ResourceMessage("org.jahia.security.license.DateValidator.invalidDate.label", nowDate, licenseDate);
            return false;
        }
    }

    public boolean assertInRange(String fromValue, String toValue) {
        Date fromLicenseDate;
        Date toLicenseDate;
        try {
            fromLicenseDate = dateFormat.parse(fromValue);
        } catch (ParseException e) {
            errorMessage = new ResourceMessage("org.jahia.security.license.DateValidator.invalidLicenseDateValue.label", fromValue);
            return false;
        }

        try {
            toLicenseDate = dateFormat.parse(toValue);
        } catch (ParseException e) {
            errorMessage = new ResourceMessage("org.jahia.security.license.DateValidator.invalidLicenseDateValue.label", toValue);
            return false;
        }

        if ((fromLicenseDate.compareTo(nowDate) <= 0) &&
            (toLicenseDate.compareTo(nowDate) >= 0)) {
            return true;
        }

        errorMessage = new ResourceMessage("org.jahia.security.license.DateValidator.dateNotInRange.label", nowDate, fromLicenseDate, toLicenseDate);
        return false;
    }

}
