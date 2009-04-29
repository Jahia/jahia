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