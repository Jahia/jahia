/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.module;

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.widget.calendar.CalendarPanel;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.extjs.gxt.ui.client.GXT;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 31, 2008
 * Time: 2:35:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarJahiaModule extends JahiaModule {
    public String getJahiaModuleType() {
        return JahiaType.CALENDAR;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        GXT.init();
        try {
            for (RootPanel actionPane : rootPanels) {
                String callback = DOM.getElementAttribute(actionPane.getElement(), "callback");
                String activeDate = DOM.getElementAttribute(actionPane.getElement(), "activedate");
                String datePattern = DOM.getElementAttribute(actionPane.getElement(), "datepattern");
                boolean displayTime = Boolean.parseBoolean(DOM.getElementAttribute(actionPane.getElement(), "displaytime"));
                boolean readOnly = Boolean.parseBoolean(DOM.getElementAttribute(actionPane.getElement(), "readonly"));
                boolean shadow = Boolean.parseBoolean(DOM.getElementAttribute(actionPane.getElement(), "shadow"));
                String fieldName = DOM.getElementAttribute(actionPane.getElement(), "fieldname");
                String valueString = DOM.getElementAttribute(actionPane.getElement(), "value");
                Long value = null;
                if (valueString != null && valueString.length() > 0) {
                    value = Long.parseLong(valueString);
                }
                actionPane.add(new CalendarPanel(callback, activeDate));
            }
        } catch (Exception e) {
            Log.error("Error in DateFieldJahiaModule: " + e.getMessage(), e);
        }
    }

}
