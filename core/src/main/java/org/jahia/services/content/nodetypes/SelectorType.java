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

package org.jahia.services.content.nodetypes;

import javax.jcr.PropertyType;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 mars 2008
 * Time: 17:06:46
 * To change this template use File | Settings | File Templates.
 */
public class SelectorType {
    public static final int SMALLTEXT = 1;
    public static final int RICHTEXT = 2;
    public static final int DATETIMEPICKER = 3;
    public static final int DATEPICKER = 4;
    public static final int FILE = 9;
    public static final int CHECKBOX = 11;
    public static final int COLOR = 12;
    public static final int CATEGORY = 13;
    public static final int CHOICELIST = 14;

    public static final int PORTLET = 20;
    public static final int PORTLETDEFINITION = 22;

    public static final int PAGE = 21;

    public static final String SELECTORNAME_SMALLTEXT = "Text";
    public static final String SELECTORNAME_RICHTEXT = "RichText";
    public static final String SELECTORNAME_DATETIMEPICKER = "DateTimePicker";
    public static final String SELECTORNAME_DATEPICKER = "DatePicker";
    public static final String SELECTORNAME_FILE = "File";
    public static final String SELECTORNAME_CHECKBOX = "Checkbox";
    public static final String SELECTORNAME_COLOR = "Color";
    public static final String SELECTORNAME_CATEGORY = "Category";
    public static final String SELECTORNAME_CHOICELIST = "Choicelist";

    public static final String SELECTORNAME_PORTLET = "Portlet";
    public static final String SELECTORNAME_PORTLETDEFINITION = "PortletDefinition";
    public static final String SELECTORNAME_PAGE = "Page";

    public static final Map<Integer, Integer> defaultSelectors = new HashMap<Integer, Integer>();
    public static final Map<String, Integer> nameToValue = new HashMap<String, Integer>();
    public static final Map<Integer, String> valueToName = new HashMap<Integer, String>();

    static {
        nameToValue.put(SELECTORNAME_SMALLTEXT, SMALLTEXT);
        nameToValue.put(SELECTORNAME_RICHTEXT, RICHTEXT);
        nameToValue.put(SELECTORNAME_DATETIMEPICKER, DATETIMEPICKER);
        nameToValue.put(SELECTORNAME_DATEPICKER, DATEPICKER);
        nameToValue.put(SELECTORNAME_FILE, FILE);
        nameToValue.put(SELECTORNAME_CHECKBOX, CHECKBOX);
        nameToValue.put(SELECTORNAME_COLOR, COLOR);
        nameToValue.put(SELECTORNAME_CATEGORY, CATEGORY);
        nameToValue.put(SELECTORNAME_CHOICELIST, CHOICELIST);

        nameToValue.put(SELECTORNAME_PORTLET, PORTLET);
        nameToValue.put(SELECTORNAME_PORTLETDEFINITION, PORTLETDEFINITION);
        nameToValue.put(SELECTORNAME_PAGE, PAGE);

        valueToName.put(SMALLTEXT, SELECTORNAME_SMALLTEXT);
        valueToName.put(RICHTEXT, SELECTORNAME_RICHTEXT);
        valueToName.put(DATETIMEPICKER, SELECTORNAME_DATETIMEPICKER);
        valueToName.put(DATEPICKER, SELECTORNAME_DATEPICKER);
        valueToName.put(FILE, SELECTORNAME_FILE);
        valueToName.put(CHECKBOX, SELECTORNAME_CHECKBOX);
        valueToName.put(COLOR, SELECTORNAME_COLOR);
        valueToName.put(CATEGORY, SELECTORNAME_CATEGORY);
        valueToName.put(CHOICELIST, SELECTORNAME_CHOICELIST);

        valueToName.put(PORTLET, SELECTORNAME_PORTLET);
        valueToName.put(PORTLETDEFINITION, SELECTORNAME_PORTLETDEFINITION);
        valueToName.put(PAGE, SELECTORNAME_PAGE);

        defaultSelectors.put(PropertyType.STRING, SMALLTEXT);
        defaultSelectors.put(PropertyType.LONG, SMALLTEXT);
        defaultSelectors.put(PropertyType.DOUBLE, SMALLTEXT);
        defaultSelectors.put(PropertyType.DATE, DATETIMEPICKER);
        defaultSelectors.put(PropertyType.BOOLEAN, CHECKBOX);
        defaultSelectors.put(PropertyType.NAME, SMALLTEXT);
        defaultSelectors.put(PropertyType.PATH, SMALLTEXT);
    }

    public static String nameFromValue(int i) {
        return valueToName.get(i);
    }

    public static int valueFromName(String s) {
        return nameToValue.get(s);        
    }

}
