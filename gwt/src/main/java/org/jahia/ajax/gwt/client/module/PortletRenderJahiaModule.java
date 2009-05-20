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

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.widget.portlet.PortletRender;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Dec 4, 2008
 * Time: 3:03:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortletRenderJahiaModule extends JahiaModule {
    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {

        for (RootPanel portletRenderPane : rootPanels) {
            String entryPointInstanceID = DOM.getElementAttribute(portletRenderPane.getElement(), "entryPointInstanceID");
            String windowID = DOM.getElementAttribute(portletRenderPane.getElement(), "windowID");
            String pathInfo = DOM.getElementAttribute(portletRenderPane.getElement(), "pathInfo");
            String queryString = DOM.getElementAttribute(portletRenderPane.getElement(), "queryString");
            portletRenderPane.add(new PortletRender(page, windowID, entryPointInstanceID, pathInfo, queryString));
        }

    }

    public String getJahiaModuleType() {
        return JahiaType.PORTLET_RENDER;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
