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
package org.jahia.ajax.gwt.client.service;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 15 ao�t 2008
 * Time: 11:22:45
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaContentService extends RemoteService {

    /**
     * Utility/Convinience class.
     * Use JahiaService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static JahiaContentServiceAsync ourInstance = null;


        public static synchronized JahiaContentServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"content/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (JahiaContentServiceAsync) GWT.create(JahiaContentService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }
            return ourInstance;
        }

    }

    public GWTJahiaContainerList loadContainerList(GWTJahiaPageContext page, String containerListName);

    public String insertAddContainerHref(GWTJahiaPageContext page);

    public void saveContainerProperty(GWTJahiaPageContext page, int containerId, String propertyName, String propertyValue);

    public List<GWTJahiaUserProperty> getJahiaUserProperties(boolean onlyMySettings);

    public GWTJahiaAjaxActionResult updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties);

    public String getContent(GWTJahiaPageContext page, int containerId);

    public String getFieldValues(int containerId, String field);

    public GWTJahiaContainer loadContainer(int containerId);

    public String getPagePropertyValue(GWTJahiaPageContext page, String propertyName);

    public void updatePagePropertyValue(GWTJahiaPageContext page, String propertyName, String propertyValue);

    public GWTJahiaPageWrapper getSiteHomePage(int siteId);

    public GWTJahiaPageWrapper getPage(int pid);

    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(int pid, String mode, GWTJahiaPageWrapper parentPage);

    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(int parent);

    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(GWTJahiaPageWrapper parentPage);

    public GWTJahiaPageWrapper getHomePageForCurrentUser(int pid, String mode, boolean recursive);

}
