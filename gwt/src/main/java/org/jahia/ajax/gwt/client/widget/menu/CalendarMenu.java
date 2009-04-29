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
package org.jahia.ajax.gwt.client.widget.menu;

import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;

import java.util.Date;

/**
 * @author Khue NGuyen
 */
public class CalendarMenu extends Menu {

    /**
     * The internal date picker.
     */
    protected CalendarPicker picker;

    private CalendarMenuItem item;

    public CalendarMenu(Date date) {
        item = new CalendarMenuItem(date);
        picker = item.picker;
        add(item);
        baseStyle = "x-date-menu";
        setAutoHeight(true);
    }

    public CalendarMenu() {
        item = new CalendarMenuItem();
        picker = item.picker;
        add(item);
        baseStyle = "x-date-menu";
        setAutoHeight(true);
    }

    /**
     * Returns the selected date.
     *
     * @return the date
     */
    public Date getDate() {
        return item.picker.getValue();
    }

    public void setDate(Date date) {
        if (date == null || this.picker == null) {
            return;
        }
        this.picker.setValue(date);
    }

    /**
     * Displays this menu relative to another element.
     *
     * @param elem the element to align to
     * @param pos the {@link El#alignTo} anchor position to use in aligning to the
     *          element (defaults to defaultAlign)
     */
    public void show(Element elem, String pos) {
        this.picker.hide();
        super.show(elem, pos);
        this.picker.show();
    }

    /**
     * Returns the date picker.
     *
     * @return the date picker
     */
    public CalendarPicker getDatePicker() {
        return picker;
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(item.picker);
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(item.picker);
    }

}
