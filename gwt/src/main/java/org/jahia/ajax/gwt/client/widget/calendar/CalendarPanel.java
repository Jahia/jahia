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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.google.gwt.user.client.Event;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 31, 2008
 * Time: 3:24:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarPanel extends LayoutContainer {
    public CalendarPanel(final String callback, String activeDate) {
        setLayout(new FlowLayout());
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
        final DateMenu menu = new DateMenu();
        if(activeDate !=null && !"".equals(activeDate.trim()))
        menu.getDatePicker().setValue(dateTimeFormat.parse(activeDate),true);
        menu.addListener(Event.ONKEYDOWN+Event.ONKEYUP, new Listener() {
            public void handleEvent(ComponentEvent ce){
                nativeCallback(callback, dateTimeFormat.format(menu.getDate()));
            }

            public void handleEvent(BaseEvent x0) {
                handleEvent((ComponentEvent)x0);
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
