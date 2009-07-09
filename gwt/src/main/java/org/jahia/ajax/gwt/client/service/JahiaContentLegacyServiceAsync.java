/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.service;

import java.util.List;

import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 15 ao�t 2008
 * Time: 11:23:49
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaContentLegacyServiceAsync {

    void loadContainerList(GWTJahiaPageContext page, String containerListName, AsyncCallback<GWTJahiaContainerList> async);

    void insertAddContainerHref(GWTJahiaPageContext page, AsyncCallback<String> async);

    void saveContainerProperty(GWTJahiaPageContext page, int containerId, String propertyName, String propertyValue, AsyncCallback async);

    void getContent(GWTJahiaPageContext page, int containerId, AsyncCallback<String> async);

    void getFieldValues(int containerId, String field, AsyncCallback<String> async);

    void loadContainer(int containerId, AsyncCallback<GWTJahiaContainer> async);

    void getPagePropertyValue(GWTJahiaPageContext page, String propertyName, AsyncCallback<String> async);

    void getJahiaUserProperties(boolean onlyMySettings,AsyncCallback<List<GWTJahiaUserProperty>> async);

    void updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties,AsyncCallback<GWTJahiaAjaxActionResult> async);

    void updatePagePropertyValue(GWTJahiaPageContext page, String propertyName, String propertyValue, AsyncCallback async);

    void getSiteHomePage(int siteId, AsyncCallback<GWTJahiaPageWrapper> async);

    void getPage(int pid, AsyncCallback<GWTJahiaPageWrapper> async);

    void getSubPagesForCurrentUser(int pid, String mode, GWTJahiaPageWrapper parentPage, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getSubPagesForCurrentUser(int parent, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getSubPagesForCurrentUser(GWTJahiaPageWrapper parentPage, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getHomePageForCurrentUser(int pid, String mode, boolean recursive, AsyncCallback<GWTJahiaPageWrapper> async);

    void searchInPages(String queryString, AsyncCallback<List<GWTJahiaPageWrapper>> async);

}
