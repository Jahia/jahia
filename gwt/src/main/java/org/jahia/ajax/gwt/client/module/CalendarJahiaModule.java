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
