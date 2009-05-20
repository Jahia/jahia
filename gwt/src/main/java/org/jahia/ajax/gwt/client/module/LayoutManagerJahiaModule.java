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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;

import java.util.List;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:35:39
 */
public class LayoutManagerJahiaModule extends JahiaModule {
    public static final String LAYOUT_MANAGER_ID = "layoutManager";
    public static final String LAYOUT_MANAGER_WIDTH = "lm-with";

    public String getJahiaModuleType() {
        return JahiaType.LAYOUT_MANAGER;
    }

    public void onModuleLoad(final GWTJahiaPageContext jahiaPageContext, final List<RootPanel> rootPanels) {
        if (rootPanels != null && rootPanels.size() == 1) {
            // get Root element attributes
            LayoutmanagerService.App.getInstance().getLayoutmanagerConfig(new AsyncCallback() {
                public void onSuccess(Object o) {
                    GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = (GWTJahiaLayoutManagerConfig) o;
                    if (o == null || gwtLayoutManagerConfig.getNbColumns() < 1) {
                        gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
                        gwtLayoutManagerConfig.setNbColumns(3);
                        gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
                        gwtLayoutManagerConfig.setLiveDraggable(true);
                    }


                    JahiaPortalManager jahiaPortal = new JahiaPortalManager(gwtLayoutManagerConfig);
                    rootPanels.get(0).add(jahiaPortal);
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error while loading portlet", throwable);
                    // load a defautl config
                    GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
                    gwtLayoutManagerConfig.setNbColumns(3);
                    gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
                    gwtLayoutManagerConfig.setLiveDraggable(true);
                    JahiaPortalManager jahiaPortal = new JahiaPortalManager(gwtLayoutManagerConfig);
                    rootPanels.get(0).add(jahiaPortal);
                }
            });
        }
    }


}
