package org.jahia.ajax.gwt.client.widget.poller;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskEvent implements Serializable {
    private Integer numberOfTasks;

    public TaskEvent() {
    }

    public Integer getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(Integer numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }
}

