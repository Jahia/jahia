/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow.jbpm;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;

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
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, null,
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, null,
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

}