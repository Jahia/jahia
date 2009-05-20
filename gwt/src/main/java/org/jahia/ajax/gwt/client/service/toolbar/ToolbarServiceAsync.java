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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.analytics.GWTJahiaAnalyticsParameter;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;

import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:57:22
 */
public interface ToolbarServiceAsync {
    public void getGWTToolbars(GWTJahiaPageContext pageContext,boolean reset, AsyncCallback<GWTJahiaToolbarSet> async);

    public void loadGWTToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar, AsyncCallback<GWTJahiaToolbar> async);

    public void updateToolbars(GWTJahiaPageContext pageContext, List<GWTJahiaToolbar> toolbarList, AsyncCallback async);

    public void updateToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar, AsyncCallback async);

    public void execute(GWTJahiaPageContext pageContext, Map<String, GWTJahiaProperty> gwtPropertiesMap, AsyncCallback<GWTJahiaAjaxActionResult> async);

    public void updateGWTJahiaStateInfo(GWTJahiaPageContext pageContext,GWTJahiaStateInfo gwtJahiaStateInfo, AsyncCallback<GWTJahiaStateInfo> async);

    public void quickValidate(String objectKey, String lang, String action, String comment, AsyncCallback async);

    public void quickAddToBatch(String objectKey, String lang, String action, AsyncCallback async);

    public void publishAll(String comment, AsyncCallback async) ;

    public void getGAdata(GWTJahiaAnalyticsParameter p,AsyncCallback<Map<String, String>> async);

    public void getGAsiteProperties(int pid,AsyncCallback<Map<String, String>> async);

    public void isTracked(AsyncCallback<Boolean> async);

   

}
