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
package org.jahia.ajax.gwt.client.service.layoutmanager;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;

import java.util.List;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:24:33
 */
public interface LayoutmanagerService extends RemoteService {


    /**
     * Utility/Convinience class.
     * Use JahiaService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static LayoutmanagerServiceAsync ourInstance = null;


        public static synchronized LayoutmanagerServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "layoutmanager/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (LayoutmanagerServiceAsync) GWT.create(LayoutmanagerService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }

    }

    public String saveLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem draggableWidgetPreferences) throws GWTJahiaServiceException;

    public void saveAsDefault(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException;

    public void restoreDefault(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException;

    public void removeLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem draggableWidgetPreferences) throws GWTJahiaServiceException;

    public void saveLayoutItems(GWTJahiaPageContext pageContext, List<GWTJahiaLayoutItem> layoutItems) throws GWTJahiaServiceException;

    public void addLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem layoutItem) throws GWTJahiaServiceException;    

    public GWTJahiaLayoutManagerConfig getLayoutmanagerConfig() throws GWTJahiaServiceException;

    public void saveLayoutmanagerConfig(GWTJahiaPageContext pageContext, GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig) throws GWTJahiaServiceException;

    public List<GWTJahiaLayoutItem> getLayoutItems(GWTJahiaPageContext page) throws GWTJahiaServiceException;

}
