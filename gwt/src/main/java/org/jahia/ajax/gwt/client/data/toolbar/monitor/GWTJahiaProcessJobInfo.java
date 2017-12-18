/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
