/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
