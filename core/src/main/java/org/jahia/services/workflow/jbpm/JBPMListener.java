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
package org.jahia.services.workflow.jbpm;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.event.process.*;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * An process event listener to synchronize the JCR nodes process ID storage with the current state of a process
 * instance (started, ended).
 * Date: Feb 4, 2010
 * Time: 8:04:51 PM
 */
public class JBPMListener extends DefaultProcessEventListener {
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 665473577321892992L;

    private static final Logger logger = LoggerFactory.getLogger(JBPMListener.class);

    private JBPM6WorkflowProvider provider;
    private WorkflowObservationManager observationManager;

    public JBPMListener(JBPM6WorkflowProvider provider) {
        this.provider = provider;
    }

    public void setObservationManager(WorkflowObservationManager observationManager) {
        this.observationManager = observationManager;
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        super.afterProcessStarted(event);    // call any default behavior first
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) event.getProcessInstance();
        final List<String> ids = (List<String>) workflowProcessInstance.getVariable("nodeIds");
        String workspace = (String) workflowProcessInstance.getVariable("workspace");
        final long executionId = workflowProcessInstance.getId();

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null,
                    new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            if (ids != null) {
                                for (String id : ids) {
                                    JCRNodeWrapper node = null;
                                    ItemNotFoundException previousException = null;
                                    try {
                                        node = session.getNodeByUUID(id);
                                    } catch (ItemNotFoundException e) {
                                        previousException = e;
                                    }
                                    if (previousException != null) {
                                        throw previousException;
                                    }
                                    provider.getWorkflowService().addProcessId(node,
                                            provider.getKey(), Long.toString(executionId));

                                }
                            }
                            return true;
                        }
                    });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        if (observationManager != null) {
            observationManager.notifyWorkflowStarted(provider.getKey(), Long.toString(executionId));
        }

    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) event.getProcessInstance();
        final List<String> ids = (List<String>) workflowProcessInstance.getVariable("nodeIds");
        String workspace = (String) workflowProcessInstance.getVariable("workspace");
        final long executionId = workflowProcessInstance.getId();

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null,
                    new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            if (ids != null) {
                                for (String id : ids) {
                                    JCRNodeWrapper node = null;
                                    ItemNotFoundException previousException = null;
                                    try {
                                        node = session.getNodeByUUID(id);
                                    } catch (ItemNotFoundException e) {
                                        previousException = e;
                                    }
                                    if (node != null) {
                                        provider.getWorkflowService().removeProcessId(node,
                                                provider.getKey(), Long.toString(executionId));
                                    } else {
                                        logger.warn(
                                                "A workflow process may have been partially ended because this node cannot be found: {}",
                                                previousException.getMessage());
                                    }
                                }
                            }
                            return true;
                        }
                    });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        if (observationManager != null) {
            observationManager.notifyWorkflowEnded(provider.getKey(), Long.toString(executionId));
        }

        super.afterProcessCompleted(event);    //call default behavior last
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        logger.debug("Before node '{}' triggered {}, variables={}", event.getNodeInstance().getNodeName(), event, getVariables(event));
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        logger.debug("After node '{}' triggered {}, variables={}", event.getNodeInstance().getNodeName(), event, getVariables(event));
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        logger.debug("Variable changed {}",event);
    }

    private Map<String,Object> getVariables(ProcessNodeTriggeredEvent event) {
        if (event.getProcessInstance() instanceof WorkflowProcessInstanceImpl) {
            return ((WorkflowProcessInstanceImpl) event.getProcessInstance()).getVariables();
        }
        return null;
    }
}
