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

/**
 * Simple range limit
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RangeLimitValue extends LimitValue {
    
    private final static String RANGE_SEPERATOR = " to ";

    private String fromValue = null;
    private String toValue = null;
    
    public RangeLimitValue(String value) {
        super(value);
        int rangeSepPos = value.indexOf(RANGE_SEPERATOR);
        if (rangeSepPos == -1) {
            this.fromValue = value;
            this.toValue = null;
        } else {
            this.fromValue = value.substring(0, rangeSepPos).trim();
            this.toValue = value.substring(rangeSepPos + RANGE_SEPERATOR.length()).trim();
        }
    }
    
    public static boolean isRangeValue(String value) {
        return (value.indexOf(RANGE_SEPERATOR) != -1);
    }
    
    /**
     * @return
     */
    public String getFromValue() {
        return fromValue;
    }

    /**
     * @return
     */
    public String getToValue() {
        return toValue;
    }

    public boolean check(Validator validator) {
        return validator.assertInRange(fromValue, toValue);
    }
    
}
