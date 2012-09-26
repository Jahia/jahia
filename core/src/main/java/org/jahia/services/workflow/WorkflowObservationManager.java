package org.jahia.services.workflow;

import java.util.ArrayList;
import java.util.List;

public class WorkflowObservationManager {
    private WorkflowService service;
    private List<WorkflowListener> listeners = new ArrayList<WorkflowListener>();

    public WorkflowObservationManager(WorkflowService service) {
        this.service = service;
    }

    public void notifyWorkflowStarted(String provider, String workflowId) {
        Workflow wf = service.getWorkflow(provider, workflowId, null);
        for (WorkflowListener listener : listeners) {
            try {
                listener.workflowStarted(wf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyWorkflowEnded(String provider, String workflowId) {
        HistoryWorkflow wf = service.getHistoryWorkflow(workflowId, provider, null);
        for (WorkflowListener listener : listeners) {
            try {
                listener.workflowEnded(wf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyNewTask(String provider, String taskId) {
        WorkflowTask task = service.getWorkflowTask(taskId, provider,null);
        for (WorkflowListener listener : listeners) {
            try {
                listener.newTaskCreated(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyTaskEnded(String provider, String taskId) {
        WorkflowTask task = service.getWorkflowTask(taskId, provider,null);
        for (WorkflowListener listener : listeners) {
            try {
                listener.taskEnded(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addWorkflowListener(WorkflowListener listener) {
        listeners.add(listener);
    }


}
