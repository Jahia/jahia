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

package org.jahia.ajax.gwt.templates.components.rss.client;

import java.util.List;

import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.templates.commons.client.ui.WidgetElement;
import org.jahia.ajax.gwt.templates.components.rss.client.ui.RSSPanel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 4:27:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSJahiaModule extends JahiaModule {


    public String getJahiaModuleType() {
        return JahiaType.RSS;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        for (RootPanel pane : rootPanels) {

            String url = DOM.getElementAttribute(pane.getElement(), "url");
            String count = DOM.getElementAttribute(pane.getElement(), "entriesCount");
            if (url != null) {
                int rssPerPage = 5;
                try {
                    rssPerPage = Integer.parseInt(count);
                } catch (NumberFormatException e) {
                    Log.error(e.getMessage(), e);
                }
                pane.add(new RSSPanel(page, url, rssPerPage));
            }
        }
    }

    public static void loadRSS(String elementId, String url, String numberPage) {
        RootPanel.get(elementId).add(new RSSPanel(JahiaGWTParameters.getGWTJahiaPageContext(), url, 5));
        return;
    }

   


}
