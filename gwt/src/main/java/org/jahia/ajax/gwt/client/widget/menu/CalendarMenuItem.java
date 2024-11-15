/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

