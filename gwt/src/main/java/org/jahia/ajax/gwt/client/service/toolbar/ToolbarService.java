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
package org.jahia.ajax.gwt.client.service.toolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.analytics.GWTJahiaAnalyticsParameter;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;

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

    public GWTJahiaToolbarSet getGWTToolbars(GWTJahiaPageContext page,boolean reset) throws GWTJahiaServiceException;

    public void updateToolbars(GWTJahiaPageContext page, List<GWTJahiaToolbar> toolbarList) throws GWTJahiaServiceException;

    public void updateToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar)  throws GWTJahiaServiceException;

    public GWTJahiaAjaxActionResult execute(GWTJahiaPageContext page, Map<String, GWTJahiaProperty> gwtPropertiesMap)throws GWTJahiaServiceException;

    public GWTJahiaStateInfo updateGWTJahiaStateInfo(GWTJahiaPageContext pageContext,GWTJahiaStateInfo gwtJahiaStateInfo)throws GWTJahiaServiceException;
    
    public GWTJahiaToolbar loadGWTToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar);

    public void quickValidate(String objectKey, String lang, String action, String comment) throws GWTJahiaServiceException;

    public void quickAddToBatch(String objectKey, String lang, String action) throws GWTJahiaServiceException;

    public void publishAll(String comment) throws GWTJahiaServiceException ;

    public Map<String, String> getGAdata(GWTJahiaAnalyticsParameter p) throws GWTJahiaServiceException;

    public Map<String, String> getGAsiteProperties(int pid);

    public boolean isTracked();


}
