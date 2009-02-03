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

package org.jahia.ajax.gwt.templates.components.toolbar.client;

import java.util.List;

import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.commons.client.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.ToolbarManager;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:24:16
 */
public class ToolbarJahiaModule extends JahiaModule {
    private static String ACTUAL_CONTENT = "actualContent";
    private RootPanel topPanel;

    public String getJahiaModuleType() {
        return JahiaType.TOOLBARS_MANAGER;
    }

    public void onModuleLoad(GWTJahiaPageContext pageContext, List<RootPanel> jahiaTypePanels) {
        if (jahiaTypePanels != null && jahiaTypePanels.size() > 1) {
            Log.warn("There are several toolbar jahiaType elements. Only the first is handled");
        }

        // create toolbar manager
        final ToolbarManager toolbarManagerWidget;
        if (JahiaPageEntryPoint.getToolbarManager() == null) {
            toolbarManagerWidget = new ToolbarManager(jahiaTypePanels.get(0), pageContext);
        } else {
            toolbarManagerWidget = JahiaPageEntryPoint.getToolbarManager();
        }

        // load toolbar
        toolbarManagerWidget.createUI();

        JahiaPageEntryPoint.setToolbarManager(toolbarManagerWidget);

    }


}
