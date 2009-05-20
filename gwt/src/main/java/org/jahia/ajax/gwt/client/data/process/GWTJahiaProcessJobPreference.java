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
package org.jahia.ajax.gwt.client.data.process;

import java.io.Serializable;
import java.util.List;

/**
 * User: jahia
 * Date: 28 juil. 2008
 * Time: 16:23:03
 */
public class GWTJahiaProcessJobPreference implements Serializable {
    public static int PREF_GENERAL = 1;
    public static int PREF_FILTER = 2;
    private int dataType;
    private int maxJobs = 100;
    private int jobsPerPage = 50;
    private List<String> jobsTypeToIgnore;
    private List<String> jobsStatusToIgnore;
    private boolean onlyCurrentUser;
    private boolean autoRefresh;
    private boolean refreshAtEndOfAnyPageWorkflow;
    private int pageRefreshNotification;

    public GWTJahiaProcessJobPreference() {
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public boolean isRefreshAtEndOfAnyPageWorkflow() {
        return refreshAtEndOfAnyPageWorkflow;
    }

    public void setRefreshAtEndOfAnyPageWorkflow(boolean refreshAtEndOfAnyPageWorkflow) {
        this.refreshAtEndOfAnyPageWorkflow = refreshAtEndOfAnyPageWorkflow;
    }

    public int getJobsPerPage() {
        return jobsPerPage;
    }

    public void setJobsPerPage(int jobsPerPage) {
        this.jobsPerPage = jobsPerPage;
    }

    public List<String> getJobsStatusToIgnore() {
        return jobsStatusToIgnore;
    }

    public void setJobsStatusToIgnore(List<String> jobsStatusToIgnore) {
        this.jobsStatusToIgnore = jobsStatusToIgnore;
    }

    public List<String> getJobsTypeToIgnore() {
        return jobsTypeToIgnore;
    }

    public void setJobsTypeToIgnore(List<String> jobsTypeToIgnore) {
        this.jobsTypeToIgnore = jobsTypeToIgnore;
    }

    public int getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(int maxJobs) {
        this.maxJobs = maxJobs;
    }

    public boolean isOnlyCurrentUser() {
        return onlyCurrentUser;
    }

    public void setOnlyCurrentUser(boolean onlyCurrentUser) {
        this.onlyCurrentUser = onlyCurrentUser;
    }

    public int getPageRefreshNotification() {
        return pageRefreshNotification;
    }

    public void setPageRefreshNotification(int pageRefreshNotification) {
        this.pageRefreshNotification = pageRefreshNotification;
    }
}
