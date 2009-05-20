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

import com.google.gwt.user.client.ui.*;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.widget.sitemap.SitemapTree;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SitemapJahiaModule extends JahiaModule {
    public static final String SITEMAP_ID = "default_sitemap";

     public String getJahiaModuleType() {
        return JahiaType.SITEMAP;
    }

    public void onModuleLoad(final GWTJahiaPageContext page, final List<RootPanel> rootPanels) {

        if (rootPanels != null && rootPanels.size() == 1) {

            RootPanel rootPanel = rootPanels.get(0) ;

            // init panel
            final LayoutContainer sitemap = new LayoutContainer(new FitLayout());
            sitemap.add(new SitemapTree(page)) ;
            DockPanel panel = new DockPanel() ;
            panel.add(sitemap, DockPanel.CENTER) ;
            rootPanel.add(panel);
        }

    }

    public List<RootPanel> getRootPanels() {
        List<RootPanel> panels = new ArrayList<RootPanel>() ;
        panels.add(RootPanel.get(SITEMAP_ID)) ;
        return panels ;
    }


}