/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
