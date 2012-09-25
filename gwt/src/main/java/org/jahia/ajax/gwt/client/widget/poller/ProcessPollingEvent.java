package org.jahia.ajax.gwt.client.widget.poller;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;

import java.io.Serializable;
import java.util.ArrayList;

public class ProcessPollingEvent implements Serializable {
    private ArrayList<GWTJahiaJobDetail> activeJobs;

    public ProcessPollingEvent() {
    }

    public ArrayList<GWTJahiaJobDetail> getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(ArrayList<GWTJahiaJobDetail> activeJobs) {
        this.activeJobs = activeJobs;
    }

}

