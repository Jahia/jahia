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

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaGroup;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaUser;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.config.client.util.URL;

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

    public PagingLoadResult<GWTJahiaGroup> searchGroups(String match, int offset, int limit);

    public String[] getFormattedPrincipal(String key, char type, String[] textpattern);

}
