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
package org.jahia.ajax.gwt.client.widget.calendar;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.Date;

/**
 * Helper class for instantiating date picker controls in the page.
 *  
 * @author Xavier Lawrence
 */
public class DatePickerManager {

    public static final String DATE_FIELD_TYPE = "DateField";

    public DatePickerManager() {
        try {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                RootPanel root = RootPanel.get(DATE_FIELD_TYPE + i);
                if (root == null) break;

                String datePattern = DOM.getElementAttribute(root.getElement(), "datepattern");
                boolean displayTime = Boolean.parseBoolean(DOM.getElementAttribute(root.getElement(), "displaytime"));
                boolean readOnly = Boolean.parseBoolean(DOM.getElementAttribute(root.getElement(), "readonly"));
                boolean shadow = Boolean.parseBoolean(DOM.getElementAttribute(root.getElement(), "shadow"));
                String fieldName = DOM.getElementAttribute(root.getElement(), "fieldname");
                String valueString = DOM.getElementAttribute(root.getElement(), "value");
                Date value = null;
                if (valueString != null && valueString.length() > 0) {
                    value = new Date(Long.parseLong(valueString));
                }

                root.add(new CalendarField(datePattern, displayTime, readOnly, fieldName, shadow, value));
            }
        } catch (Exception e) {
            Log.error("Error in DatePickerManager: " + e.getMessage(), e);
        }
    }
}
