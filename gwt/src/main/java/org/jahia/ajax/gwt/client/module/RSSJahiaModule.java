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
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.widget.rss.RSSPanel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 4:27:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSJahiaModule extends JahiaModule {


    public String getJahiaModuleType() {
        return JahiaType.RSS;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        for (RootPanel pane : rootPanels) {

            String url = DOM.getElementAttribute(pane.getElement(), "url");
            String count = DOM.getElementAttribute(pane.getElement(), "entriesCount");
            if (url != null) {
                int rssPerPage = 5;
                try {
                    rssPerPage = Integer.parseInt(count);
                } catch (NumberFormatException e) {
                    Log.error(e.getMessage(), e);
                }
                pane.add(new RSSPanel(page, url, rssPerPage));
            }
        }
    }

    public static void loadRSS(String elementId, String url, String numberPage) {
        RootPanel.get(elementId).add(new RSSPanel(JahiaGWTParameters.getGWTJahiaPageContext(), url, 5));
        return;
    }

   


}
