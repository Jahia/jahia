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
