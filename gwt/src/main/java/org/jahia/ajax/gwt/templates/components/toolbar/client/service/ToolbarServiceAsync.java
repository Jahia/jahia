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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbars;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.analytics.GWTAnalyticsParameter;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.monitor.GWTJahiaStateInfo;

import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:57:22
 */
public interface ToolbarServiceAsync {
    public void getGWTToolbars(GWTJahiaPageContext pageContext,boolean reset, AsyncCallback<GWTToolbars> async);

    public void loadGWTToolbar(GWTJahiaPageContext pageContext, GWTToolbar gwtToolbar, AsyncCallback<GWTToolbar> async);

    public void updateToolbars(GWTJahiaPageContext pageContext, List<GWTToolbar> toolbarList, AsyncCallback async);

    public void updateToolbar(GWTJahiaPageContext pageContext, GWTToolbar gwtToolbar, AsyncCallback async);

    public void execute(GWTJahiaPageContext pageContext, Map<String, GWTProperty> gwtPropertiesMap, AsyncCallback<GWTAjaxActionResult> async);

    public void updateGWTJahiaStateInfo(GWTJahiaPageContext pageContext,GWTJahiaStateInfo gwtJahiaStateInfo, AsyncCallback<GWTJahiaStateInfo> async);

    public void quickValidate(String objectKey, String lang, String action, String comment, AsyncCallback async);

    public void quickAddToBatch(String objectKey, String lang, String action, AsyncCallback async);

    public void publishAll(String comment, AsyncCallback async) ;

    public void getGAdata(GWTAnalyticsParameter p,AsyncCallback<Map<String, String>> async);

    public void getGAsiteProperties(int pid,AsyncCallback<Map<String, String>> async);

    public void isTracked(AsyncCallback<Boolean> async);

   

}
