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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.List;

/**
 *
 *
 * User: hollis
 * Date: 25 juil. 2008 - 12:40:05
 */
public interface UserManagerService extends RemoteService  {

    public static class App {
        private static UserManagerServiceAsync serv = null;

        public static synchronized UserManagerServiceAsync getInstance() {
            if (serv == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"userManager/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                serv = (UserManagerServiceAsync) GWT.create(UserManagerService.class);
                ((ServiceDefTarget) serv).setServiceEntryPoint(serviceEntryPoint);
            }
            return serv;
        }
    }

    public PagingLoadResult<GWTJahiaUser> searchUsers (String match, int offset, int limit, List<Integer> siteIds);

    public PagingLoadResult<GWTJahiaGroup> searchGroups(String match, int offset, int limit, List<Integer> siteIds);

    public PagingLoadResult<GWTJahiaUser> searchUsersInContext (String match, int offset, int limit, String context);

    public PagingLoadResult<GWTJahiaGroup> searchGroupsInContext (String match, int offset, int limit, String context);


    public String[] getFormattedPrincipal(String key, char type, String[] textpattern);

}
