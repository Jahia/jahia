/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import javax.jcr.PropertyType;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 * User: toto
 * Date: 14 mars 2008
 * Time: 17:06:46
 * 
 */
public class SelectorType {
    public static final int SMALLTEXT = 1;
    public static final int RICHTEXT = 2;
    public static final int DATETIMEPICKER = 3;
    public static final int DATEPICKER = 4;
    public static final int TEXTAREA = 5;
    public static final int CONTENTPICKER = 9;
    public static final int FILEUPLOAD = 10;
    public static final int CHECKBOX = 11;
    public static final int COLOR = 12;
    public static final int CATEGORY = 13;
    public static final int CHOICELIST = 14;
    public static final int CRON = 15;
    public static final int TAG = 16;

    public static final int PAGE = 21;

    public static final String SELECTORNAME_SMALLTEXT = "Text";
    public static final String SELECTORNAME_RICHTEXT = "RichText";
    public static final String SELECTORNAME_DATETIMEPICKER = "DateTimePicker";
    public static final String SELECTORNAME_DATEPICKER = "DatePicker";
    public static final String SELECTORNAME_TEXTAREA = "TextArea";
    public static final String SELECTORNAME_CONTENTPICKER = "Picker";
    public static final String SELECTORNAME_FILEUPLOAD = "FileUpload";
    public static final String SELECTORNAME_CHECKBOX = "Checkbox";
    public static final String SELECTORNAME_COLOR = "Color";
    public static final String SELECTORNAME_CATEGORY = "Category";
    public static final String SELECTORNAME_CHOICELIST = "Choicelist";
    public static final String SELECTORNAME_CRON = "Cron";
    public static final String SELECTORNAME_TAG = "Tag";

    public static final Map<Integer, Integer> defaultSelectors = new HashMap<Integer, Integer>();
    public static final Map<String, Integer> nameToValue = new HashMap<String, Integer>();
    public static final Map<Integer, String> valueToName = new HashMap<Integer, String>();

    static {
        nameToValue.put(SELECTORNAME_SMALLTEXT, SMALLTEXT);
        nameToValue.put(SELECTORNAME_RICHTEXT, RICHTEXT);
        nameToValue.put(SELECTORNAME_DATETIMEPICKER, DATETIMEPICKER);
        nameToValue.put(SELECTORNAME_DATEPICKER, DATEPICKER);
        nameToValue.put(SELECTORNAME_TEXTAREA, TEXTAREA);
        nameToValue.put(SELECTORNAME_CONTENTPICKER, CONTENTPICKER);
        nameToValue.put(SELECTORNAME_FILEUPLOAD, FILEUPLOAD);
        nameToValue.put(SELECTORNAME_CHECKBOX, CHECKBOX);
        nameToValue.put(SELECTORNAME_COLOR, COLOR);
        nameToValue.put(SELECTORNAME_CATEGORY, CATEGORY);
        nameToValue.put(SELECTORNAME_CHOICELIST, CHOICELIST);
        nameToValue.put(SELECTORNAME_CRON, CRON);
        nameToValue.put(SELECTORNAME_TAG, TAG);

        valueToName.put(SMALLTEXT, SELECTORNAME_SMALLTEXT);
        valueToName.put(RICHTEXT, SELECTORNAME_RICHTEXT);
        valueToName.put(DATETIMEPICKER, SELECTORNAME_DATETIMEPICKER);
        valueToName.put(DATEPICKER, SELECTORNAME_DATEPICKER);
        valueToName.put(TEXTAREA, SELECTORNAME_TEXTAREA);
        valueToName.put(CONTENTPICKER, SELECTORNAME_CONTENTPICKER);
        valueToName.put(FILEUPLOAD, SELECTORNAME_FILEUPLOAD);
        valueToName.put(CHECKBOX, SELECTORNAME_CHECKBOX);
        valueToName.put(COLOR, SELECTORNAME_COLOR);
        valueToName.put(CATEGORY, SELECTORNAME_CATEGORY);
        valueToName.put(CHOICELIST, SELECTORNAME_CHOICELIST);
        valueToName.put(CRON, SELECTORNAME_CRON);
        valueToName.put(TAG, SELECTORNAME_TAG);

        defaultSelectors.put(PropertyType.STRING, SMALLTEXT);
        defaultSelectors.put(PropertyType.LONG, SMALLTEXT);
        defaultSelectors.put(PropertyType.DOUBLE, SMALLTEXT);
        defaultSelectors.put(PropertyType.DATE, DATETIMEPICKER);
        defaultSelectors.put(PropertyType.BOOLEAN, CHECKBOX);
        defaultSelectors.put(PropertyType.NAME, SMALLTEXT);
        defaultSelectors.put(PropertyType.PATH, SMALLTEXT);
        defaultSelectors.put(PropertyType.WEAKREFERENCE, CONTENTPICKER);
    }

    public static String nameFromValue(int i) {
        return valueToName.get(i);
    }

    public static int valueFromName(String s) {
        return nameToValue.get(s);        
    }

    public static Map<String, Integer> getNameToValue() {
        return nameToValue;
    }

    public static Map<Integer, String> getValueToName() {
        return valueToName;
    }
}
