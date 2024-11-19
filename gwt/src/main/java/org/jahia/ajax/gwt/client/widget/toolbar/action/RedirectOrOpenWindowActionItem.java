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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;

import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 10:45:02
 */
public class RedirectOrOpenWindowActionItem extends OpenWindowActionItem {


    @Override
    public void handleNewLinkerSelection() {
        Map preferences = getGwtToolbarItem().getProperties();
        final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(Constants.URL);
        if (windowUrl != null && windowUrl.getValue() != null) {
            try {
                URL.replacePlaceholders(windowUrl.getValue(), linker.getSelectionContext().getSingleSelection());
                setEnabled(true);
            } catch (Exception e) {
                setEnabled(false);
            }
        }
    }

    public void onComponentSelection() {
        String jsUrl = getPropertyValue(getGwtToolbarItem(), "js.url");
        if (jsUrl != null) {
            Window.Location.assign(JahiaGWTParameters.getParam(jsUrl));
        } else {
            Map preferences = getGwtToolbarItem().getProperties();
            final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(Constants.URL);
            if (windowUrl != null && windowUrl.getValue() != null) {
                Window.Location.assign(URL.replacePlaceholders(windowUrl.getValue(), linker.getSelectionContext().getSingleSelection()));
            }
        }
    }

    @Override
    public Component getTextToolItem() {
        Component c = super.getTextToolItem();
        Menu m = new Menu();
        MenuItem menuItem = new MenuItem(Messages.get("label.openInNewWindow", "Open in new window"));
        menuItem.setIcon(ToolbarIconProvider.getInstance().getIcon("openWindow"));
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                RedirectOrOpenWindowActionItem.super.onComponentSelection();
            }
        });
        m.add(menuItem);
        c.setContextMenu(m);
        return c;
    }
}
