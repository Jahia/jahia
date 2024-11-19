/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Get workflow definition from name
 */
public class GetWorkflowsForDefinitionCommand extends BaseCommand<List<Workflow>> {
    private final String definition;
    private final Locale uiLocale;

    public GetWorkflowsForDefinitionCommand(String definition, Locale uiLocale) {
        this.definition = definition;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        final List<Workflow> workflows = new LinkedList<Workflow>();
        for (org.kie.api.definition.process.Process process : getKieSession().getKieBase().getProcesses()) {
            Collection<ProcessInstanceLog> processInstanceLogs = getLogService().findActiveProcessInstances(process.getId());
            for (ProcessInstanceLog processInstanceLog : processInstanceLogs) {
                ProcessInstance processInstance = getKieSession().getProcessInstance(processInstanceLog.getProcessInstanceId());
                if (processInstance != null && processInstance.getProcessName().equals(definition) && processInstance instanceof WorkflowProcessInstance) {
                    workflows.add(convertToWorkflow(processInstance, uiLocale, getKieSession(), getTaskService(), getLogService()));
                }
            }
        }
        return workflows;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n definition: %s", definition) +
                String.format("%n uiLocale: %s", uiLocale);
    }
}
