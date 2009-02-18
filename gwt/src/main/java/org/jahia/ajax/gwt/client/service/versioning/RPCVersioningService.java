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

package org.jahia.ajax.gwt.client.service.versioning;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionComparisonData;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;

/**
 *
 */
public interface RPCVersioningService extends RemoteService {

    /**
     * Utility/Convinience class.
     * Use RPCVersionComparisonService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static RPCVersioningServiceAsync ourInstance = null;


        public static synchronized RPCVersioningServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "versioning/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (RPCVersioningServiceAsync) GWT.create(RPCVersioningService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }
            return ourInstance;
        }

    }

    public GWTJahiaVersionComparisonData getData(GWTJahiaPageContext page,String versionableUUID, String version1, String version2,
                                         String lang);

    /**
     * Returns revisions entries with or without staging entry
     *
     * @param page
     * @param versionableUUID
     * @param lang
     * @param withStagingRevision if true, return staging revision too
     * @param withDeletedRevision if true, return deleted revision too
     * @param applyLanguageFiltering
     * @param skipNotAvailablePageRevisions
     * @param offset
     * @param sortParameter
     * @param isAscending
     * @param versionPerPage
     * @return
     */
    public PagingLoadResult<GWTJahiaVersion> getRevisions(GWTJahiaPageContext page,String versionableUUID,
                                        String lang, boolean withStagingRevision, boolean withDeletedRevision,
                                        boolean applyLanguageFiltering, boolean skipNotAvailablePageRevisions,
                                        int offset, String sortParameter, boolean isAscending, int versionPerPage);

}