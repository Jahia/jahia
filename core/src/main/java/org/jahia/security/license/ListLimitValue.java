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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jahia.resourcebundle.ResourceMessage;

/**
 * List of values limit
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ListLimitValue extends LimitValue {

    private static final String LIST_SEPERATOR = ";";

    private List limitValues = new ArrayList();
    private boolean lastCheckResult = false;

    public ListLimitValue(String value) {
        super(value);
        int listSepPos = value.indexOf(LIST_SEPERATOR);
        int curStrPos = 0;
        while (listSepPos != -1) {
            String curLimitStr = value.substring(curStrPos, listSepPos);
            LimitValue subLimit = null;
            if (RangeLimitValue.isRangeValue(curLimitStr)) {
                subLimit = new RangeLimitValue(curLimitStr);
            } else {
                subLimit = new LimitValue(curLimitStr);
            }
            limitValues.add(subLimit);
            curStrPos = listSepPos + LIST_SEPERATOR.length();
            listSepPos = value.indexOf(LIST_SEPERATOR, curStrPos);
        }
        // we must still process the values until the end of the string.
        String curLimitStr = value.substring(curStrPos);
        LimitValue subLimit = null;
        if (RangeLimitValue.isRangeValue(curLimitStr)) {
            subLimit = new RangeLimitValue(curLimitStr);
        } else {
            subLimit = new LimitValue(curLimitStr);
        }
        limitValues.add(subLimit);
    }

    public static boolean isListValue(String value) {
        return (value.indexOf(LIST_SEPERATOR) != -1);
    }

    /**
     * @return
     */
    public List getLimitValues() {
        return limitValues;
    }

    public boolean check(Validator validator) {
        Iterator valueIter = limitValues.iterator();
        while (valueIter.hasNext()) {
            LimitValue curLimitValue = (LimitValue) valueIter.next();
            if (curLimitValue.check(validator)) {
                lastCheckResult = true;
                return true;
            }
        }
        lastCheckResult = false;
        return false;
    }

    public ResourceMessage getErrorMessage(Validator validator) {
        if (lastCheckResult == true) {
            return null;
        }
        Iterator valueIter = limitValues.iterator();
        while (valueIter.hasNext()) {
            LimitValue curLimitValue = (LimitValue) valueIter.next();
            ResourceMessage curResourceMessage = curLimitValue.getErrorMessage(validator);
            if (curResourceMessage != null) {
                return curResourceMessage;
            }
        }
        return null;
    }

}
