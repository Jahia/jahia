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
