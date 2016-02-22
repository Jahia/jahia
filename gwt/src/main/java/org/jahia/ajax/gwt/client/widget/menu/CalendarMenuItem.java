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
package org.jahia.ajax.gwt.client.widget.menu;

import java.util.Date;

import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.google.gwt.user.client.Element;

/**
 * 
 * User: hollis
 * Date: 17 juil. 2008
 * Time: 14:02:05
 * 
 */
public class CalendarMenuItem extends Item {

    protected CalendarPicker picker;

    /**
     * Creates a new menu item.
     */
    public CalendarMenuItem(Date date) {
        hideOnClick = true;
        // date picker
        picker = new CalendarPicker(date) {
            @Override
            protected void onRender(Element target, int index) {
                super.onRender(target, index);
            }

            @Override
            public void setElement(Element element) {
                super.setElement(element);
            }
        };
        picker.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                parentMenu.fireEvent(Events.Select, ce);
                parentMenu.hide();
            }
        });
    }

    /**
     * Creates a new menu item.
     */
    public CalendarMenuItem() {
        this(null);
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        picker.render(target, index);
        setElement(picker.getElement());
    }

    @Override
    protected void handleClick(ComponentEvent be) {
        picker.onComponentEvent(be);
    }

}

