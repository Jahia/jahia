/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow.jbpm.command;

import com.google.common.base.Joiner;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
* Get processes based on list of ids
*/
public class GetActiveWorkflowsInformationsCommand extends BaseCommand<List<Workflow>> {

	private transient static Logger logger = LoggerFactory.getLogger(GetActiveWorkflowsInformationsCommand.class);
	
    private final List<String> processIds;
    private final Locale uiLocale;

    public GetActiveWorkflowsInformationsCommand(List<String> processIds, Locale uiLocale) {
        this.processIds = processIds;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        List<Workflow> activeWorkflows = new ArrayList<Workflow>();
        for (String processId : processIds) {
        	ProcessInstance processInstance = getKieSession().getProcessInstance(Long.parseLong(processId));
        	if (processInstance != null) {
                activeWorkflows.add(convertToWorkflow(processInstance, uiLocale, getKieSession(), getTaskService(), getLogService()));
        	} else {
        		logger.debug("Retrieving process instance with ID {} returned null while getting active workflows", processId);
        	}
        }
        return activeWorkflows;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n processIds: %s", Joiner.on(",").join(processIds)) +
                String.format("%n uiLocale: %s", uiLocale);
    }
}
