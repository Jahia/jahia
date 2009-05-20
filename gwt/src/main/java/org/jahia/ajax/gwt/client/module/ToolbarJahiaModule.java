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

import java.util.List;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.widget.toolbar.ToolbarManager;

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
