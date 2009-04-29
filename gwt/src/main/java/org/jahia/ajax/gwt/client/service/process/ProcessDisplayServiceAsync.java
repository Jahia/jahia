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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;


/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 11:32:11
 * To change this template use File | Settings | File Templates.
 */
public interface ProcessDisplayServiceAsync {

    void getGWTProcessJobStat(int mode,AsyncCallback async);

    public void savePreferences(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences,AsyncCallback async);

    public void getPreferences(AsyncCallback<GWTJahiaProcessJobPreference> async);    

    void findGWTProcessJobs(int offset, String parameter, boolean isAscending, AsyncCallback<PagingLoadResult<GWTJahiaProcessJob>> async);

    void deleteJob(GWTJahiaProcessJob gwtProcessJob, AsyncCallback async);
}
