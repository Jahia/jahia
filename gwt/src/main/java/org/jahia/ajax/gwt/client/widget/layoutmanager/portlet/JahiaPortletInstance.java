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
package org.jahia.ajax.gwt.client.widget.layoutmanager.portlet;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.widget.portlet.PortletRender;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: ktlili
 * Date: 9 d�c. 2008
 * Time: 12:02:17
 */
public class JahiaPortletInstance extends JahiaPortlet {
    private PortletRender portletRender;

    public JahiaPortletInstance(GWTJahiaLayoutItem porletConfig) {
        super(porletConfig);
        setLayout(new FlowLayout());
        setStyleAttribute("background", "none");
        doView();

    }

    public JahiaPortletInstance() {
        setLayout(new FlowLayout());
        doView();
    }

    @Override
    public void doView() {
        super.doView();
        Log.debug("Path info ID: " + getPathInfo());
        setHeading(getPorletConfig().getGwtJahiaNode().getName());
        portletRender = new PortletRender(getPageContext(), "-1", getWindowID(), getPathInfo(), getQueryString()) {
            @Override
            public void onRender() {
                super.onRender();
                layout();

            }
        };
        add(portletRender);
        layout();
    }

    public void refreshContent() {
        if (portletRender != null) {
            portletRender.refresh();
        }
    }

    private String getPathInfo() {
        try {
            return JahiaGWTParameters.getPathInfo();
        } catch (Exception e) {
            return "";
        }
    }

    private String getQueryString() {
        return URL.getQueryString();
    }

    @Override
    public void doEdit() {
        super.doEdit();
    }

    @Override
    public void doHelp() {
        super.doHelp();
    }

    public GWTJahiaPageContext getPageContext() {
        return JahiaGWTParameters.getGWTJahiaPageContext();
    }

    public String getWindowID() {
        return getPorletConfig().getPortlet();
    }


}
