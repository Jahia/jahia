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

package org.jahia.ajax.gwt.templates.components.toolbar.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.config.client.util.URL;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.commons.client.rpc.GWTJahiaServiceException;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbars;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.analytics.GWTAnalyticsParameter;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.monitor.GWTJahiaStateInfo;

import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:31:45
 */
public interface ToolbarService extends RemoteService {
    /**
     * Utility/Convinience class.
     * Use PdisplayServiceAsync.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static ToolbarServiceAsync ourInstance = null;


        public static synchronized ToolbarServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "toolbar/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (ToolbarServiceAsync) GWT.create(ToolbarService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }                                                                

    }

    public GWTToolbars getGWTToolbars(GWTJahiaPageContext page,boolean reset) throws GWTJahiaServiceException;  

    public void updateToolbars(GWTJahiaPageContext page, List<GWTToolbar> toolbarList) throws GWTJahiaServiceException;

    public void updateToolbar(GWTJahiaPageContext pageContext, GWTToolbar gwtToolbar)  throws GWTJahiaServiceException;

    public GWTAjaxActionResult execute(GWTJahiaPageContext page, Map<String, GWTProperty> gwtPropertiesMap)throws GWTJahiaServiceException;

    public GWTJahiaStateInfo updateGWTJahiaStateInfo(GWTJahiaPageContext pageContext,GWTJahiaStateInfo gwtJahiaStateInfo)throws GWTJahiaServiceException;
    
    public GWTToolbar loadGWTToolbar(GWTJahiaPageContext pageContext,GWTToolbar gwtToolbar);

    public void quickValidate(String objectKey, String lang, String action, String comment) throws GWTJahiaServiceException;

    public void publishAll(String comment) throws GWTJahiaServiceException ;

    public Map<String, String> getGAdata(GWTAnalyticsParameter p) throws GWTJahiaServiceException;

    public Map<String, String> getGAsiteProperties(int pid);

    public boolean isTracked();


}
