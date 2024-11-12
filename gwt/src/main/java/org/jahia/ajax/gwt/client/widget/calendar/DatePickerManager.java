/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import java.util.Date;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

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
