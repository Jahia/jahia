/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.layoutmanager.portlet;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.widget.portlet.PortletRender;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

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
        renderPortlet();

    }

    public JahiaPortletInstance() {
        setLayout(new FlowLayout());
        renderPortlet();
    }

    @Override
    public void doView() {
        super.doView();
    }

    private void renderPortlet() {
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
        return getPorletConfig().getNode();
    }


}
