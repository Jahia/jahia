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
package org.jahia.ajax.gwt.client.service.process;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobStat;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

/**
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 11:31:13
 */
public interface ProcessDisplayService extends RemoteService {
    /**
     * Utility/Convinience class.
     * Use PdisplayServiceAsync.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static ProcessDisplayServiceAsync ourInstance = null;


        public static synchronized ProcessDisplayServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "pdisplay/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (ProcessDisplayServiceAsync) GWT.create(ProcessDisplayService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }

    }

    public GWTJahiaProcessJobStat getGWTProcessJobStat(int mode);

    public void savePreferences(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences);

    public GWTJahiaProcessJobPreference getPreferences() throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaProcessJob> findGWTProcessJobs(int offset, String parameter, boolean isAscending) throws GWTJahiaServiceException ;

    void deleteJob(GWTJahiaProcessJob gwtProcessJob);
}
