/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.pipelines.Pipeline;
import org.jahia.services.workflow.jbpm.custom.JahiaLocalHTWorkItemHandler;
import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.runtime.manager.impl.KModuleRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.DisposeListener;
import org.kie.internal.task.api.EventService;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom version of the KModuleRegisterableItemsFactory that enables us to register our own
 * HTWorkItemHandler implementation
 */
public class JahiaKModuleRegisterableItemsFactory extends KModuleRegisterableItemsFactory {

    private Pipeline peopleAssignmentPipeline = null;

    public JahiaKModuleRegisterableItemsFactory(KieContainer kieContainer, String ksessionName) {
        super(kieContainer, ksessionName);
    }

    public JahiaKModuleRegisterableItemsFactory(KieContainer kieContainer, String ksessionName, AuditEventBuilder auditBuilder) {
        super(kieContainer, ksessionName, auditBuilder);
    }

    public JahiaKModuleRegisterableItemsFactory(KieContainer kieContainer, String ksessionName, Pipeline peopleAssignmentPipeline) {
        super(kieContainer, ksessionName);
        this.peopleAssignmentPipeline = peopleAssignmentPipeline;
    }

    public JahiaKModuleRegisterableItemsFactory(KieContainer kieContainer, String ksessionName, AuditEventBuilder auditBuilder, Pipeline peopleAssignmentPipeline) {
        super(kieContainer, ksessionName, auditBuilder);
        this.peopleAssignmentPipeline = peopleAssignmentPipeline;
    }

    @Override
    protected WorkItemHandler getHTWorkItemHandler(RuntimeEngine runtime) {
        ExternalTaskEventListener listener = new ExternalTaskEventListener();
        listener.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());

        JahiaLocalHTWorkItemHandler humanTaskHandler = new JahiaLocalHTWorkItemHandler();
        humanTaskHandler.setPeopleAssignmentPipeline(peopleAssignmentPipeline);
        humanTaskHandler.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
        if (runtime.getTaskService() instanceof EventService) {
            ((EventService) runtime.getTaskService()).registerTaskLifecycleEventListener(listener);
        }

        if (runtime instanceof Disposable) {
            ((Disposable) runtime).addDisposeListener(new DisposeListener() {

                @Override
                public void onDispose(RuntimeEngine runtime) {
                    if (runtime.getTaskService() instanceof EventService) {
                        ((EventService) runtime.getTaskService()).clearTaskLifecycleEventListeners();
                        ((EventService) runtime.getTaskService()).clearTasknotificationEventListeners();
                    }
                }
            });
        }
        return humanTaskHandler;
    }

    @Override
    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
        List<ProcessEventListener> defaultListeners = new ArrayList<ProcessEventListener>();
        defaultListeners.addAll(super.getProcessEventListeners(runtime));
        for (ProcessEventListener defaultListener : defaultListeners) {
            if (defaultListener instanceof AbstractAuditLogger) {
                defaultListeners.remove(defaultListener);
                break;
            }
        }
        // register JPAWorkingMemoryDBLogger
        runtime.getKieSession().getEnvironment().set("IS_JTA_TRANSACTION", false);
        AbstractAuditLogger logger = new JPAWorkingMemoryDbLogger(runtime.getKieSession());
        logger.setBuilder(getAuditBuilder());
        defaultListeners.add(logger);
        // add any custom listeners
        return defaultListeners;
    }
}
