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

import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.client.data.GWTJahiaUserProperty;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 15 ao�t 2008
 * Time: 11:23:49
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaContentServiceAsync {

    void loadContainerList(GWTJahiaPageContext page, String containerListName, AsyncCallback async);

    void insertAddContainerHref(GWTJahiaPageContext page, AsyncCallback async);

    void saveContainerProperty(GWTJahiaPageContext page, int containerId, String propertyName, String propertyValue, AsyncCallback async);

    void getContent(GWTJahiaPageContext page, int containerId, AsyncCallback async);

    void getFieldValues(int containerId, String field, AsyncCallback async);

    void loadContainer(int containerId, AsyncCallback async);

    void getPagePropertyValue(GWTJahiaPageContext page, String propertyName, AsyncCallback async);

    void getJahiaUserProperties(boolean onlyMySettings,AsyncCallback async);

    void updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties,AsyncCallback async);

    void updatePagePropertyValue(GWTJahiaPageContext page, String propertyName, String propertyValue, AsyncCallback async);

    void getSiteHomePage(int siteId, AsyncCallback<GWTJahiaPageWrapper> async);

    void getPage(int pid, AsyncCallback<GWTJahiaPageWrapper> async);

    void getSubPagesForCurrentUser(int pid, String mode, GWTJahiaPageWrapper parentPage, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getSubPagesForCurrentUser(int parent, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getSubPagesForCurrentUser(GWTJahiaPageWrapper parentPage, AsyncCallback<List<GWTJahiaPageWrapper>> async);

    void getHomePageForCurrentUser(int pid, String mode, boolean recursive, AsyncCallback<GWTJahiaPageWrapper> async);

    void searchInPages(String queryString, AsyncCallback<List<GWTJahiaPageWrapper>> async);

}
