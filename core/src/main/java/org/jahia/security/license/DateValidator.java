/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
import org.jahia.utils.DateUtils;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DateValidator extends AbstractValidator {

    SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT);
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

    public long getDate() {
        long licenseDate = 0;
        try {
            licenseDate = dateFormat.parse(value).getTime();
        } catch (ParseException e) {
            errorMessage = new ResourceMessage(
                    "org.jahia.security.license.DateValidator.invalidLicenseDateValue.label",
                    value);
        }

        return licenseDate;
    }
}
