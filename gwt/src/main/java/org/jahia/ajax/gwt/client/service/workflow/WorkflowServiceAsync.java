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

import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowHistoryEntry;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowManagerState;
import org.jahia.ajax.gwt.client.data.GWTJahiaLabel;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowBatch;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 17 juil. 2008 - 16:01:05
 */
public interface WorkflowServiceAsync {

    public void getAvailableLanguages(AsyncCallback<List<String>> async) ;

    public void getSubElements(GWTJahiaWorkflowElement parent, AsyncCallback<List<GWTJahiaWorkflowElement>> async);

    public void getFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth, AsyncCallback<List<GWTJahiaWorkflowElement>> async) ;

    public void getPagedFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth, int offset, int pageSize, String sortParameter, boolean isAscending, AsyncCallback<PagingLoadResult<GWTJahiaWorkflowElement>> async) ;

    public void getAvailableActions(AsyncCallback<List<GWTJahiaLabel>> async);

    public void storeBatch(Map<String, Map<String, Set<String>>> batch, AsyncCallback async);

    public void restoreBatch(AsyncCallback<Map<String, Map<String, Set<String>>>> async);

    public void executeStoredBatch(String name, String comment, AsyncCallback async);

    public void execute(GWTJahiaWorkflowBatch batch, AsyncCallback async);

    public void getHistory(GWTJahiaWorkflowElement item, AsyncCallback<ListLoadResult<GWTJahiaWorkflowHistoryEntry>> async);

    public void saveWorkflowManagerState(GWTJahiaWorkflowManagerState state, AsyncCallback async);

    public void getWorkflowManagerState(AsyncCallback<GWTJahiaWorkflowManagerState> async) ;

}
