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

/**
 * User: Serge Huber
 * Date: 4 janv. 2006
 * Time: 17:57:59
 * Copyright (C) Jahia Inc.
 */
public class BooleanValidator extends AbstractValidator {

    public BooleanValidator (String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals (String value) {
        Boolean boolValue = Boolean.valueOf(value);
        return Boolean.TRUE.equals(boolValue);
    }

    public boolean assertInRange (String fromValue, String toValue) {
        Boolean fromBool = Boolean.valueOf(fromValue);
        Boolean toBool = Boolean.valueOf(toValue);
        if (fromBool == null) {
            return false;
        }
        if (fromBool.equals(toBool)) {
            return Boolean.TRUE.equals(fromBool);
        } else {
            return false;
        }
    }
}
