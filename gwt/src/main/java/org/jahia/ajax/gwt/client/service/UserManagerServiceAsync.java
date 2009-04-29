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

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 juil. 2008
 * Time: 12:39:05
 * To change this template use File | Settings | File Templates.
 */
public interface UserManagerServiceAsync {

    void searchUsers (String match, int offset, int limit, List<Integer> siteIds, AsyncCallback<PagingLoadResult<GWTJahiaUser>> async);

    void searchUsersInContext (String match, int offset, int limit, String context, AsyncCallback<PagingLoadResult<GWTJahiaUser>> async);

    void searchGroups(String match, int offset, int limit, List<Integer> siteIds, AsyncCallback<PagingLoadResult<GWTJahiaGroup>> async);

    void searchGroupsInContext (String match, int offset, int limit, String context, AsyncCallback<PagingLoadResult<GWTJahiaGroup>> async);

    void getFormattedPrincipal(String userkey, char type, String[] textpattern, AsyncCallback<String[]> async);
}
