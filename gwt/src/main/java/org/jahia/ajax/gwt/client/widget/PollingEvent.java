package org.jahia.ajax.gwt.client.widget;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;

import java.io.Serializable;
import java.util.ArrayList;

public class PollingEvent  implements Serializable {
    private String type;
    private ArrayList<GWTJahiaJobDetail> activeJobs;
    private Integer numberOfTasks;

    public PollingEvent() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<GWTJahiaJobDetail> getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(ArrayList<GWTJahiaJobDetail> activeJobs) {
        this.activeJobs = activeJobs;
    }

    public Integer getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(Integer numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }
}

