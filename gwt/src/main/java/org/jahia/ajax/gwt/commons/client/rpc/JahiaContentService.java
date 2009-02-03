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

package org.jahia.ajax.gwt.commons.client.rpc;

import org.jahia.ajax.gwt.commons.client.beans.*;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.config.client.util.URL;
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

    public GWTAjaxActionResult updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties);

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
