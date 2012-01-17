/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
