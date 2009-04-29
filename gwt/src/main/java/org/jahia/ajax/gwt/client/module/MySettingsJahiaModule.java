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
import org.jahia.ajax.gwt.client.widget.mysettings.MySettingsPanel;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.google.gwt.user.client.ui.RootPanel;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 16:17:32
 */
public class MySettingsJahiaModule extends JahiaModule {
     public String getJahiaModuleType() {
        return JahiaType.MY_SETTINGS;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        try {
            for (RootPanel rootPanel : rootPanels) {
                rootPanel.add(new MySettingsPanel(rootPanel,this));
            }
        } catch (Exception e) {
            Log.error("Error in DateFieldJahiaModule: " + e.getMessage(), e);
        }
    }
}
