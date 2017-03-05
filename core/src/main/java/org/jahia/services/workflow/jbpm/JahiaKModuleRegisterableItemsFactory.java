/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
