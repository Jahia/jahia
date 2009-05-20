/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.toolbar.monitor;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 7 ao�t 2008
 * Time: 14:37:39
 */
public class GWTJahiaProcessJobInfo implements Serializable {
    private int numberJobs;
    private int numberWaitingJobs;
    private int nextJobCurrentUserIndex;
    private String nextJobCurrentUserType;
    private long lastViewTime;
    private String lastTitle ;
    private boolean jobExecuting;
    private boolean jobFinished;
    private boolean autoRefresh;
    private boolean currentUserJob;
    private boolean systemJob;
    private boolean currentPageValidated;
    private String jobType;
    private String jobReportUrl;


    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public boolean isJobExecuting() {
        return jobExecuting;
    }

    public void setJobExecuting(boolean jobExecuting) {
        this.jobExecuting = jobExecuting;
    }

    public long getLastViewTime() {
        return lastViewTime;
    }

    public void setLastViewTime(long lastViewTime) {
        this.lastViewTime = lastViewTime;
    }

    public boolean isJobFinished() {
        return jobFinished;
    }

    public void setJobFinished(boolean jobFinished) {
        this.jobFinished = jobFinished;
    }

    public int getNextJobCurrentUserIndex() {
        return nextJobCurrentUserIndex;
    }

    public void setNextJobCurrentUserIndex(int nextJobCurrentUserIndex) {
        this.nextJobCurrentUserIndex = nextJobCurrentUserIndex;
    }

    public String getNextJobCurrentUserType() {
        return nextJobCurrentUserType;
    }

    public void setNextJobCurrentUserType(String nextJobCurrentUserType) {
        this.nextJobCurrentUserType = nextJobCurrentUserType;
    }

    public int getNumberJobs() {
        return numberJobs;
    }

    public void setNumberJobs(int numberJobs) {
        this.numberJobs = numberJobs;
    }

    public int getNumberWaitingJobs() {
        return numberWaitingJobs;
    }

    public void setNumberWaitingJobs(int numberWaitingJobs) {
        this.numberWaitingJobs = numberWaitingJobs;
    }

    public boolean isCurrentUserJob() {
        return currentUserJob;
    }

    public void setCurrentUserJob(boolean currentUserJob) {
        this.currentUserJob = currentUserJob;
    }

    public boolean isSystemJob() {
        return systemJob;
    }

    public void setSystemJob(boolean systemJob) {
        this.systemJob = systemJob;
    }

    public boolean isCurrentPageValidated() {
        return currentPageValidated;
    }

    public void setCurrentPageValidated(boolean currentPageValidated) {
        this.currentPageValidated = currentPageValidated;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobReportUrl() {
        return jobReportUrl;
    }

    public void setJobReportUrl(String jobReportUrl) {
        this.jobReportUrl = jobReportUrl;
    }

    public String getLastTitle() {
        return lastTitle;
    }

    public void setLastTitle(String lastTitle) {
        this.lastTitle = lastTitle;
    }
}
