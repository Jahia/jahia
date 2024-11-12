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
package org.jahia.ajax.gwt.client.widget.menu;

import java.util.Date;

import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.widget.menu.Menu;

/**
 * @author Khue NGuyen
 */
public class CalendarMenu extends Menu {

    /**
     * The internal date picker.
     */
    protected CalendarPicker picker;

    public CalendarMenu() {
        this(null);
    }

    public CalendarMenu(Date date) {
        super();
        picker = new CalendarPicker(date);
        add(picker);
        addStyleName("x-date-menu");
        setAutoHeight(true);
        plain = true;
        showSeparator = false;
        setEnableScrolling(false);
    }

    /**
     * Returns the selected date.
     *
     * @return the date
     */
    public Date getDate() {
        return picker.getValue();
    }

    public CalendarPicker getDatePicker() {
        return picker;
    }

    protected void onClick(ComponentEvent ce) {
//        hide(true);
    }

    public void setDate(Date date) {
        if (date != null && this.picker != null) {
            this.picker.setValue(date);
        }
    }

    @Override
    protected boolean onAutoHide(PreviewEvent pe) {
        return pe.getTarget(".x-datetime-selector", 5) == null ? super.onAutoHide(pe) : false;
    }

}
