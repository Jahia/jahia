/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"workflow.gwt";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (WorkflowServiceAsync) GWT.create(WorkflowService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
            }
            return app;
        }
    }

    public List<String> getAvailableLanguages();

    public List<GWTJahiaWorkflowElement> getSubElements(GWTJahiaWorkflowElement parent) throws GWTJahiaServiceException;

    public List<GWTJahiaWorkflowElement> getFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth);

    public PagingLoadResult<GWTJahiaWorkflowElement> getPagedFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth, int offset, int pageSize, String sortParameter, boolean isAscending) throws GWTJahiaServiceException;

    public List<GWTJahiaLabel> getAvailableActions();

    public void storeBatch(Map<String, Map<String, Set<String>>> batch);

    public Map<String, Map<String, Set<String>>> restoreBatch();

    public void executeStoredBatch(String name, String comment);

    public void execute(GWTJahiaWorkflowBatch batch);

    public ListLoadResult<GWTJahiaWorkflowHistoryEntry> getHistory(GWTJahiaWorkflowElement item);

    public void saveWorkflowManagerState(GWTJahiaWorkflowManagerState state);

    public GWTJahiaWorkflowManagerState getWorkflowManagerState();
    
    public String getPreviewLink(String objectKey, boolean compareMode, String languageCode) throws GWTJahiaServiceException;

}
