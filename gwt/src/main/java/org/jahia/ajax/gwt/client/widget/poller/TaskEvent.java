package org.jahia.ajax.gwt.client.widget.poller;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTask;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskEvent implements Serializable {
    private Integer numberOfTasks;
    private GWTJahiaWorkflowTask newTask;

    public TaskEvent() {
    }

    public Integer getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(Integer numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }

    public GWTJahiaWorkflowTask getNewTask() {
        return newTask;
    }

    public void setNewTask(GWTJahiaWorkflowTask newTask) {
        this.newTask = newTask;
    }
}

