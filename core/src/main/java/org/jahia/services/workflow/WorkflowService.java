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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class WorkflowService {
    private transient static Logger logger = Logger.getLogger(WorkflowService.class);


    private Map<String, WorkflowProvider> providers;
    private static WorkflowService instance;

    public static WorkflowService getInstance() {
        if (instance == null) {
            instance = new WorkflowService();
        }
        return instance;
    }

    public void setProviders(Map<String,WorkflowProvider> providers) {
        this.providers = providers;
    }

    public Map<String, WorkflowProvider> getProviders() {
        return providers;
    }

    /**
     * This method list all possible workflows for the specified node.
     * @param node
     * @return A map of available workflows per provider.
     */
    public Map<String,List<Workflow>> getPossibleWorkflows (JCRNodeWrapper node) {
        Map<String,List<Workflow>> workflowsByProvider = new LinkedHashMap<String, List<Workflow>>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowsByProvider.put(providerEntry.getKey(),providerEntry.getValue().getAvailableWorlfows());
        }
        return workflowsByProvider;
    }

    /**
     * This method list all currently active workflow for the specified node.
     * @param node
     * @return A map of active workflows per provider
     */
    public Map<String,String> getActiveWorkflows(JCRNodeWrapper node) {
        return Collections.emptyMap();
    }

    /**
     * This method list all actions available at execution time for a node.
     * @param processId the process we want to advance
     * @param provider The provider executing the process
     * @return a map of actions per workflows per provider.
     */
    public List<WorkflowAction> getAvailableActions(String processId,String provider) {
        return Collections.emptyList();
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     * @param processId the process we want to advance
     * @param provider The provider executing the process
     * @param args List of args for the process
     */
    public void signalProcess(String processId,String provider,Object... args) {
        
    }
}
