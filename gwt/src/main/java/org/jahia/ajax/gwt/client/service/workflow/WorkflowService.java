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
package org.jahia.ajax.gwt.client.service.workflow;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowManagerState;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLabel;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 17 juil. 2008 - 16:00:55
 */
public interface WorkflowService extends RemoteService {

    public static class App {
        private static WorkflowServiceAsync app = null;

        public static synchronized WorkflowServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"workflow/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (WorkflowServiceAsync) GWT.create(WorkflowService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
            }
            return app;
        }
    }

    public List<String> getAvailableLanguages() ;

    public List<GWTJahiaWorkflowElement> getSubElements(GWTJahiaWorkflowElement parent) throws GWTJahiaServiceException;

    public List<GWTJahiaWorkflowElement> getFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth) ;

    public PagingLoadResult<GWTJahiaWorkflowElement> getPagedFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth, int offset, int pageSize, String sortParameter, boolean isAscending) throws GWTJahiaServiceException ;

    public List<GWTJahiaLabel> getAvailableActions();

    public void storeBatch(Map<String, Map<String, Set<String>>> batch);

    public Map<String, Map<String, Set<String>>> restoreBatch();

    public void executeStoredBatch(String name, String comment);

    public void execute(GWTJahiaWorkflowBatch batch);

    public ListLoadResult<GWTJahiaWorkflowHistoryEntry> getHistory(GWTJahiaWorkflowElement item);

    public void saveWorkflowManagerState(GWTJahiaWorkflowManagerState state);

    public GWTJahiaWorkflowManagerState getWorkflowManagerState() ;

}
