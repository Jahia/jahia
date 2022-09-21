/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.google.gwt.user.client.Event;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * 
 * User: toto
 * Date: Dec 31, 2008
 * Time: 3:24:55 PM
 * 
 */
public class CalendarPanel extends LayoutContainer {

    // TODO GXT 2

    public CalendarPanel(final String callback, String activeDate) {
        setLayout(new FlowLayout());
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
        final DateMenu menu = new DateMenu();
        if (activeDate != null && !"".equals(activeDate.trim())) {
            menu.getDatePicker().setValue(dateTimeFormat.parse(activeDate), true);
        }
        menu.addListener(new EventType(Event.ONKEYDOWN+Event.ONKEYUP), new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce){
                nativeCallback(callback, dateTimeFormat.format(menu.getDate()));
            }
        });

        add(menu.getDatePicker());

    }

    public static native void nativeCallback(String callback, String date) /*-{
        try {
            eval('$wnd.'+callback)(date);
        } catch (e) {};
    }-*/;

}
