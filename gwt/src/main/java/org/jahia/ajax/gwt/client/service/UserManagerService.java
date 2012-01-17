/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.service;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
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
        private static UserManagerServiceAsync app = null;

        public static synchronized UserManagerServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = createEntryPointUrl();
                String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                app = (UserManagerServiceAsync) GWT.create(UserManagerService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);

                JahiaGWTParameters.addUpdater(new JahiaGWTParameters.UrlUpdater() {
                    public void updateEntryPointUrl() {
                        String relativeServiceEntryPoint = createEntryPointUrl();
                        String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                        ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
                    }
                });                
            }
            return app;
        }

        private static String createEntryPointUrl() {
            return JahiaGWTParameters.getServiceEntryPoint()+"userManager.gwt?lang="+JahiaGWTParameters.getLanguage() + "&site="+JahiaGWTParameters.getSiteUUID() + "&workspace="+JahiaGWTParameters.getWorkspace();
        }
    }

    public BasePagingLoadResult<GWTJahiaUser> searchUsers (String match, int offset, int limit, List<Integer> siteIds);

    public BasePagingLoadResult<GWTJahiaGroup> searchGroups(String match, int offset, int limit, List<Integer> siteIds);

    public BasePagingLoadResult<GWTJahiaUser> searchUsersInContext (String match, int offset, int limit, String context);

    public BasePagingLoadResult<GWTJahiaGroup> searchGroupsInContext (String match, int offset, int limit, String context);


    public String[] getFormattedPrincipal(String key, char type, String[] textpattern);

}
