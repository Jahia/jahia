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
        if(activeDate !=null && !"".equals(activeDate.trim()))
        menu.getDatePicker().setValue(dateTimeFormat.parse(activeDate),true);
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
