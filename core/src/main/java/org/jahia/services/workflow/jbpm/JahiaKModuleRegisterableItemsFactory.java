package org.jahia.services.workflow.jbpm;

import org.jahia.pipelines.Pipeline;
import org.jahia.services.workflow.jbpm.custom.JahiaLocalHTWorkItemHandler;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.runtime.manager.impl.KModuleRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.DisposeListener;
import org.kie.internal.task.api.EventService;

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
}
