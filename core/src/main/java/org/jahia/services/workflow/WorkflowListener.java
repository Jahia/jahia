package org.jahia.services.workflow;

public class WorkflowListener {

    public void workflowStarted(Workflow workflow) { }

    public void workflowEnded(HistoryWorkflow workflow) { }

    public void newTaskCreated(WorkflowTask task) { }

    public void taskEnded(WorkflowTask task) { }

}
