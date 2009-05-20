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
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.node.ThumbView;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;

import java.util.List;

/**
 * @author Xavier Lawrence
 */
public class MediaGalleryJahiaModule extends JahiaModule {

    public String getJahiaModuleType() {
        return JahiaType.MEDIA_GALLERY;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        GXT.init();
        try {
            for (RootPanel panel : rootPanels) {
                String path = DOM.getElementAttribute(panel.getElement(), "path");
                String conf = DOM.getElementAttribute(panel.getElement(), "config");
                BrowserLinker linker = new BrowserLinker() ;
                final ThumbView view = new ThumbView(ManagerConfigurationFactory.getConfiguration(conf, linker)) ;
                linker.registerComponents(null, view, null, null, null); ;
                final GWTJahiaNode directory = new GWTJahiaNode(null,null, null, path, null, null, null, null, null, false, false, false, null);
                view.setContent(directory);
                panel.add(view.getComponent());
            }
        } catch (Exception e) {
            Log.error("Error loading MediaGalleryJahiaModule", e);
        }
    }
}
