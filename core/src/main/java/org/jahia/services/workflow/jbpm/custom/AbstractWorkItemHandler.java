package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.runtime.process.WorkItemHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract work item handler interface to register the work item handler with the workflow provider
 */
public abstract class AbstractWorkItemHandler implements WorkItemHandler, InitializingBean, DisposableBean {

    private JBPM6WorkflowProvider workflowProvider;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkflowProvider(JBPM6WorkflowProvider workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        workflowProvider.registerWorkItemHandler(name, this);
    }

    @Override
    public void destroy() throws Exception {
        workflowProvider.unregisterWorkItemHandler(name);
    }
}
